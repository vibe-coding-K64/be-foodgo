/**
 * Full inventory of all orders - check status distribution
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

async function main() {
  const token = await getToken();
  const docs = await listAllOrders(token);

  console.log(`Tong so don: ${docs.length}\n`);

  const groups = {};
  for (const doc of docs) {
    const fields = doc.fields || {};
    const id = doc.name.split('/').pop();
    const status = fields.status?.integerValue ?? fields.status?.doubleValue ?? '?';
    const driverId = fields.driverId?.stringValue ?? null;
    const hasDriverId = 'driverId' in fields;
    const deliveryStep = fields.deliveryStep?.stringValue ?? '(none)';
    const code = fields.code?.stringValue ?? '(no code)';

    const key = `${status}`;
    if (!groups[key]) groups[key] = [];
    groups[key].push({ id, code, deliveryStep, driverId, hasDriverId });
  }

  for (const [status, items] of Object.entries(groups).sort((a,b) => Number(a[0]) - Number(b[0]))) {
    console.log(`=== STATUS ${status} (${items.length} don) ===`);
    for (const item of items) {
      console.log(`  ${item.id} (${item.code})`);
      console.log(`    deliveryStep=${item.deliveryStep}, hasDriverId=${item.hasDriverId}, driverId=${item.driverId}`);
    }
    console.log('');
  }
}

main().catch(err => { console.error(err); process.exit(1); });
