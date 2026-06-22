/**
 * Final verification - check if the fix is needed
 */
const { GoogleAuth } = require('google-auth-library');
const path = require('path');

const SERVICE_ACCOUNT_PATH = path.join(__dirname, 'src/main/resources/firebase-service-account.json');
const PROJECT_ID = 'food-go-17a5d';

async function getToken() {
  const auth = new GoogleAuth({
    keyFile: SERVICE_ACCOUNT_PATH,
    scopes: ['https://www.googleapis.com/auth/cloud-platform'],
  });
  const client = await auth.getClient();
  const r = await client.getAccessToken();
  return r.token;
}

async function listAllOrders(token) {
  const allDocs = [];
  let pageToken = null;

  do {
    const url = new URL(`https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/orders`);
    url.searchParams.set('pageSize', '500');
    if (pageToken) url.searchParams.set('pageToken', pageToken);

    const res = await fetch(url.toString(), {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await res.json();
    if (data.documents) allDocs.push(...data.documents);
    pageToken = data.nextPageToken || null;
  } while (pageToken);

  return allDocs;
}

async function patchDocument(token, collectionId, documentId, fields) {
  const mask = Object.keys(fields).join(',');
  const url = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/${collectionId}/${documentId}?updateMask.fieldPaths=${mask}`;

  const res = await fetch(url, {
    method: 'PATCH',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ fields }),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status}: ${text.substring(0, 200)}`);
  }
  return res.json();
}

async function main() {
  const token = await getToken();
  console.log('Token OK\n');

  const docs = await listAllOrders(token);
  console.log(`Total docs in orders collection: ${docs.length}\n`);

  // Group by status and hasDriverId
  const groups = {};
  for (const doc of docs) {
    const fields = doc.fields || {};
    const id = doc.name.split('/').pop();
    const status = fields.status?.integerValue ?? fields.status?.doubleValue;
    const hasDriverId = 'driverId' in fields;
    const driverId = fields.driverId?.stringValue ?? null;
    const code = fields.code?.stringValue ?? '(no code)';

    const key = `status=${status}, hasDriverId=${hasDriverId}`;
    if (!groups[key]) groups[key] = [];
    groups[key].push({ id, code, driverId, hasDriverId });
  }

  console.log('=== Groups ===');
  for (const [key, items] of Object.entries(groups).sort()) {
    console.log(`\n${key}: ${items.length} orders`);
    for (const item of items.slice(0, 5)) {
      console.log(`  ${item.id} (${item.code}) - driverId=${item.driverId}`);
    }
    if (items.length > 5) console.log(`  ... and ${items.length - 5} more`);
  }

  // Target: status=1, hasDriverId=false
  const targetKey = 'status=1, hasDriverId=false';
  const targetDocs = groups[targetKey] || [];
  console.log(`\n=== Orders needing driverId=null: ${targetDocs.length} ===`);

  if (targetDocs.length > 0) {
    let updated = 0, failed = 0;
    for (const doc of targetDocs) {
      try {
        await patchDocument(token, 'orders', doc.id, {
          driverId: { nullValue: 'NULL_VALUE' },
        });
        console.log(`  [OK] ${doc.id} (${doc.code})`);
        updated++;
      } catch (e) {
        console.error(`  [FAIL] ${doc.id}: ${e.message}`);
        failed++;
      }
    }
    console.log(`\nFixed: ${updated}, Failed: ${failed}`);
  }
}

main().catch(err => { console.error(err); process.exit(1); });
