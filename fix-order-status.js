/**
 * Fix inconsistent order status based on deliveryStep.
 *
 * Status meaning:
 *   0 = PENDING_STORE_CONFIRMATION (cho cua hang xac nhan)
 *   1 = WAITING_DRIVER / WAITING_PICKUP (cho tai xe nhan)
 *
 * Fix: if status=1 but deliveryStep=PENDING_STORE_CONFIRMATION, set status=0
 * Fix: if status=1 but deliveryStep=WAITING_DRIVER, keep status=1 (correct)
 * Fix: if status=1 but deliveryStep=WAITING_PICKUP, keep status=1 (correct)
 *
 * Usage: node fix-order-status.js
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

async function patchDocument(token, collectionId, documentId, fields, mask) {
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

async function main() {
  const token = await getToken();
  console.log('Token OK\n');

  const docs = await listAllOrders(token);

  // Find all orders with status=1
  const status1Orders = docs.filter(doc => {
    const fields = doc.fields || {};
    const status = fields.status?.integerValue ?? fields.status?.doubleValue;
    return status === 1;
  });

  console.log(`Orders with status=1: ${status1Orders.length}\n`);

  let setTo0 = 0, keep = 0, errors = 0;

  for (const doc of status1Orders) {
    const fields = doc.fields || {};
    const id = doc.name.split('/').pop();
    const deliveryStep = fields.deliveryStep?.stringValue ?? '(none)';
    const code = fields.code?.stringValue ?? '(no code)';

    // Fix: status=1 but deliveryStep=PENDING_STORE_CONFIRMATION -> set status=0
    if (deliveryStep === 'PENDING_STORE_CONFIRMATION') {
      console.log(`[FIX] ${id} (${code}): status=1 but deliveryStep=PENDING_STORE_CONFIRMATION -> setting status=0`);
      try {
        await patchDocument(token, 'orders', id, {
          status: { integerValue: 0 },
        }, 'status');
        console.log(`  -> OK`);
        setTo0++;
      } catch (e) {
        console.error(`  -> FAIL: ${e.message}`);
        errors++;
      }
    } else {
      console.log(`[KEEP] ${id} (${code}): status=1, deliveryStep=${deliveryStep} -> no change`);
      keep++;
    }
  }

  console.log(`\n=== Summary ===`);
  console.log(`Set to status=0: ${setTo0}`);
  console.log(`Kept status=1:  ${keep}`);
  console.log(`Errors:         ${errors}`);
}

main().catch(err => { console.error(err); process.exit(1); });
