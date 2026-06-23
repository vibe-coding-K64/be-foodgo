/**
 * Script to create Firestore composite indexes via Firebase Admin REST API.
 * Usage: node create-firestore-indexes.js
 */
const { GoogleAuth } = require('google-auth-library');
const path = require('path');

const SERVICE_ACCOUNT_PATH = path.join(__dirname, 'src/main/resources/firebase-service-account.json');
const PROJECT_ID = 'food-go-17a5d';
const BASE = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)`;

async function getToken() {
  const auth = new GoogleAuth({
    keyFile: SERVICE_ACCOUNT_PATH,
    scopes: ['https://www.googleapis.com/auth/cloud-platform'],
  });
  const client = await auth.getClient();
  const r = await client.getAccessToken();
  return r.token;
}

async function api(token, method, path, body = null) {
  const opts = {
    method,
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(`${BASE}${path}`, opts);
  const data = await res.json();
  if (!res.ok) throw new Error(`${res.status}: ${JSON.stringify(data)}`);
  return data;
}

function matches(existingFields, wanted) {
  if (!existingFields || existingFields.length !== wanted.length) return false;
  return wanted.every((f, i) =>
    existingFields[i].fieldPath === f.fieldPath &&
    existingFields[i].order === f.order
  );
}

const INDEXES = [
  {
    name: 'driverId+status+deletedAt',
    fields: [
      { fieldPath: 'driverId', order: 'ASCENDING' },
      { fieldPath: 'status', order: 'ASCENDING' },
      { fieldPath: 'deletedAt', order: 'ASCENDING' },
    ],
  },
  {
    name: 'driverId+status+updatedAt',
    fields: [
      { fieldPath: 'driverId', order: 'ASCENDING' },
      { fieldPath: 'status', order: 'ASCENDING' },
      { fieldPath: 'updatedAt', order: 'DESCENDING' },
    ],
  },
];

async function main() {
  const token = await getToken();
  console.log('Token OK\n');

  // List existing
  let existing = [];
  try {
    const data = await api(token, 'GET', '/collectionGroups/orders/indexes');
    existing = data.indexes || [];
    console.log(`Found ${existing.length} existing indexes`);
  } catch (e) {
    console.warn('Could not list existing indexes:', e.message);
  }

  let created = 0, skipped = 0, failed = 0;

  for (const idx of INDEXES) {
    const fieldsStr = idx.fields.map(f => `${f.fieldPath}(${f.order})`).join(', ');
    console.log(`\nIndex: ${idx.name} [${fieldsStr}]`);

    if (existing.some(e => matches(e.fields, idx.fields))) {
      console.log('  -> SKIP (already exists)');
      skipped++;
      continue;
    }

    try {
      // Try Firestore Admin API format
      const result = await api(token, 'POST', '/collectionGroups/orders/indexes', {
        fields: idx.fields,
      });
      console.log(`  -> CREATED (id=${result.index?.indexId})`);
      created++;
    } catch (e) {
      console.error(`  -> ERROR: ${e.message}`);
      failed++;
    }
  }

  console.log(`\n=== Done: ${created} created, ${skipped} skipped, ${failed} failed ===`);
  process.exit(failed > 0 ? 1 : 0);
}

main().catch(err => { console.error(err); process.exit(1); });
