const express = require('express');
const path = require('path');
const { Pool } = require('pg');
const os = require('os');

const app = express();
const PORT = process.env.PORT || 3000;
const GREETING = process.env.GREETING || 'Hello from the Docker demo';

// Lazily configured. The app runs fine with no database at all --
// topics 001-005 never start one.
const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: process.env.DB_PORT || 5432,
  user: process.env.DB_USER || 'postgres',
  password: process.env.DB_PASSWORD || 'secret',
  database: process.env.DB_NAME || 'postgres',
  connectionTimeoutMillis: 1500,
});

app.use(express.static(path.join(__dirname, 'public')));

app.get('/api/info', async (req, res) => {
  let db = { connected: false, detail: 'no database configured' };
  try {
    const result = await pool.query('SELECT now() AS time');
    db = { connected: true, detail: `connected to ${process.env.DB_HOST || 'localhost'}`, time: result.rows[0].time };
  } catch (err) {
    db = { connected: false, detail: `cannot reach ${process.env.DB_HOST || 'localhost'}: ${err.code || err.message}` };
  }
  res.json({
    greeting: GREETING,
    hostname: os.hostname(),
    platform: `${os.type()} ${os.release()}`,
    node: process.version,
    pid: process.pid,
    db,
  });
});

app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.listen(PORT, '0.0.0.0', () => {
  console.log(`listening on port ${PORT}`);
  console.log(`hostname is ${os.hostname()}`);
});
