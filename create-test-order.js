/**
 * Script to create a test order in Firestore with correct data types.
 * Uses Firestore REST API with proper integerValue format.
 *
 * Usage: node create-test-order.js
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

async function createDocument(token, collectionId, documentId, fields) {
  const url = `${BASE}/${collectionId}/${documentId}`;
  const body = { fields };

  const res = await fetch(url, {
    method: 'PATCH',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });

  const text = await res.text();
  if (!res.ok) {
    console.error(`Raw response (${res.status}): ${text.substring(0, 500)}`);
    throw new Error(`API error: ${res.status}`);
  }

  return JSON.parse(text);
}

async function deleteDocument(token, collectionId, documentId) {
  const url = `${BASE}/${collectionId}/${documentId}`;
  const res = await fetch(url, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` },
  });
  console.log(`  Delete status: ${res.status}`);
}

async function main() {
  const token = await getToken();
  console.log('Token OK\n');

  const orderId = `test_order_${Date.now()}`;

  // Build fields with CORRECT types
  // integerValue must be a NUMBER (not a string), doubleValue must be a NUMBER
  const fields = {
    id:              { stringValue: orderId },
    userId:          { stringValue: 'user_001' },
    storeId:         { stringValue: 'store_001' },
    storeName:       { stringValue: 'Cơm Tấm Phúc Lộc Thọ' },
    code:            { stringValue: 'TEST001' },
    totalAmount:     { doubleValue: 90000.0 },
    deliveryFee:      { doubleValue: 15000.0 },
    discountAmount:   { doubleValue: 0.0 },
    finalAmount:     { doubleValue: 105000.0 },
    status:          { integerValue: 1 },       // INTEGER 1, not string!
    deliveryStep:    { stringValue: 'WAITING_DRIVER' },
    deliveryAddress: { stringValue: 'Ký túc xá UTC2, Quận 9, TP.HCM' },
    deliveryLat:     { doubleValue: 10.8446 },
    deliveryLng:     { doubleValue: 106.7975 },
    receiverName:    { stringValue: 'Test Khach Hang' },
    receiverPhone:   { stringValue: '0123456789' },
    paymentMethod:   { integerValue: 1 },
    paymentStatus:   { integerValue: 2 },
    createdAt:       { timestampValue: new Date().toISOString() },
    updatedAt:       { timestampValue: new Date().toISOString() },
  };

  // Clean up old test orders
  console.log('=== Cleaning up old test orders ===');
  const cleanupIds = ['test_order_1781947527674'];
  for (const id of cleanupIds) {
    try {
      await deleteDocument(token, 'orders', id);
      console.log(`  Deleted: ${id}`);
    } catch (e) {
      console.log(`  (skip ${id}: ${e.message})`);
    }
  }

  // Create test order
  console.log('\n=== Creating test order with INTEGER status ===');
  console.log('status field:', JSON.stringify(fields.status));
  try {
    await createDocument(token, 'orders', orderId, fields);
    console.log(`SUCCESS: Created order "${orderId}"`);
    console.log('status=1 (INTEGER), driverId=null (no field)');
    console.log('\nRefresh driver app - order should appear now!');
  } catch (e) {
    console.error('Failed:', e.message);
  }
}

main().catch(err => { console.error(err); process.exit(1); });
