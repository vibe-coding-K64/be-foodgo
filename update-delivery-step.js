/**
 * Script to backfill deliveryStep field on existing orders in Firestore.
 *
 * This is needed because deliveryStep was not being stored in Firestore previously.
 * After running the backend with the new code, new orders will have deliveryStep set correctly.
 * This script updates existing orders that don't have deliveryStep yet.
 *
 * Usage: node update-delivery-step.js
 *
 * What it does:
 * - Orders with status=2 (DELIVERING) and pickedUpAt!=null -> deliveryStep="ON_THE_WAY"
 * - Orders with status=2 (DELIVERING) and pickedUpAt==null -> deliveryStep="WAITING_PICKUP"
 * - Orders with status=3 (COMPLETED) -> deliveryStep="DELIVERED"
 * - Orders with status=4 (CANCELLED) -> deliveryStep="CANCELLED"
 * - Orders with status=1 (WAITING_DRIVER) -> deliveryStep="WAITING_PICKUP"
 */
const { GoogleAuth } = require('google-auth-library');
const path = require('path');

const SERVICE_ACCOUNT_PATH = path.join(__dirname, 'src/main/resources/firebase-service-account.json');
const PROJECT_ID = 'food-go-17a5d';
const BASE = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents`;

async function getToken() {
  const auth = new GoogleAuth({
    keyFile: SERVICE_ACCOUNT_PATH,
    scopes: ['https://www.googleapis.com/auth/cloud-platform'],
  });
  const client = await auth.getClient();
  const r = await client.getAccessToken();
  return r.token;
}

async function getDocuments(query) {
  const token = await getToken();
  const params = new URLSearchParams({
    from: ['collections'],
    select: ['__name__', 'status', 'pickedUpAt', 'deliveredAt', 'deliveryStep'],
    where: query,
    limit: '500',
  });

  let allDocs = [];
  let pageToken = null;

  do {
    const p = new URLSearchParams(params);
    if (pageToken) p.set('pageToken', pageToken);

    const res = await fetch(`${BASE}/orders?${p}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await res.json();

    if (data.documents) {
      allDocs = allDocs.concat(data.documents);
    }

    pageToken = data.nextPageToken || null;
  } while (pageToken);

  return allDocs;
}

async function updateDocument(token, docPath, fields) {
  const mask = Object.keys(fields).join(',');
  const res = await fetch(`${BASE}/${docPath}`, {
    method: 'PATCH',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      fields: {
        deliveryStep: { stringValue: fields.deliveryStep },
        updatedAt: { timestampValue: new Date().toISOString() },
      },
      updateMask: { fieldPaths: ['deliveryStep', 'updatedAt'] },
    }),
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(`${res.status}: ${JSON.stringify(data)}`);
  }
  return data;
}

function resolveDeliveryStep(doc) {
  const fields = doc.fields || {};
  const status = fields.status?.integerValue ?? fields.status?.doubleValue ?? null;

  if (status === 3) return 'DELIVERED';
  if (status === 4) return 'CANCELLED';
  if (status === 2) {
    if (fields.pickedUpAt?.timestampValue || fields.pickedUpAt?.stringValue) return 'ON_THE_WAY';
    return 'WAITING_PICKUP';
  }
  if (status === 1) return 'WAITING_PICKUP';
  return null;
}

async function main() {
  const token = await getToken();
  console.log('Token OK\n');

  // Query orders with status=2 that don't have deliveryStep
  console.log('Fetching orders with status=2 (DELIVERING) without deliveryStep...');
  let docs = await getDocuments(
    encodeURIComponent('status = 2 and deliveryStep = null')
  );
  console.log(`Found ${docs.length} orders with status=2 and no deliveryStep`);

  let updated = 0, skipped = 0, failed = 0;

  for (const doc of docs) {
    const docId = doc.name.split('/').pop();
    const step = resolveDeliveryStep(doc);

    if (!step) {
      console.log(`  [SKIP] ${docId}: could not determine deliveryStep`);
      skipped++;
      continue;
    }

    try {
      await updateDocument(token, `orders/${docId}`, { deliveryStep: step });
      console.log(`  [OK] ${docId} -> deliveryStep="${step}"`);
      updated++;
    } catch (e) {
      console.error(`  [ERROR] ${docId}: ${e.message}`);
      failed++;
    }
  }

  console.log(`\n=== Results: ${updated} updated, ${skipped} skipped, ${failed} failed ===`);
  console.log('\nDone! Restart the backend and hot-reload the Flutter app to see changes.');
  process.exit(failed > 0 ? 1 : 0);
}

main().catch(err => { console.error(err); process.exit(1); });
