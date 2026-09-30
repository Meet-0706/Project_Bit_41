const express = require('express');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

const JWT_SECRET = 'super-secret-key';
// In-memory users for demonstration (in a real app, use Postgres)
const users = [
  // Admin user pre-seeded
  { id: '00000000-0000-0000-0000-000000000000', email: 'admin@test.com', passwordHash: bcrypt.hashSync('admin123', 10), role: 'ADMIN' },
  // Customer user pre-seeded
  { id: '11111111-1111-1111-1111-111111111111', email: 'customer@test.com', passwordHash: bcrypt.hashSync('customer123', 10), role: 'CUSTOMER' }
];

app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  const user = users.find(u => u.email === email);
  if (user && bcrypt.compareSync(password, user.passwordHash)) {
    const token = jwt.sign({ id: user.id, role: user.role }, JWT_SECRET, { expiresIn: '1h' });
    res.json({ token, user: { id: user.id, email: user.email, role: user.role } });
  } else {
    res.status(401).json({ error: 'Invalid credentials' });
  }
});

app.post('/api/auth/register', (req, res) => {
  const { email, password } = req.body;
  if (users.find(u => u.email === email)) {
    return res.status(400).json({ error: 'User already exists' });
  }
  const newUser = {
    id: require('crypto').randomUUID(),
    email,
    passwordHash: bcrypt.hashSync(password, 10),
    role: 'CUSTOMER' // always customer on self-register
  };
  users.push(newUser);
  const token = jwt.sign({ id: newUser.id, role: newUser.role }, JWT_SECRET, { expiresIn: '1h' });
  res.json({ token, user: { id: newUser.id, email: newUser.email, role: newUser.role } });
});

// Endpoint for Nginx auth_request
app.get('/api/auth/validate', (req, res) => {
  const authHeader = req.headers['authorization'];
  if (!authHeader) return res.status(401).json({ error: 'No token' });
  
  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const originalUri = req.headers['x-original-uri'] || '';
    
    // Role-based access control
    // Admin routes
    if (originalUri.startsWith('/api/payment/config') && decoded.role !== 'ADMIN') {
        return res.status(403).json({ error: 'Forbidden: Admin only' });
    }
    if (originalUri.startsWith('/api/inventory') && req.method !== 'GET' && decoded.role !== 'ADMIN') {
        return res.status(403).json({ error: 'Forbidden' });
    }
    
    // Set headers for upstream services
    res.set('X-User-Id', decoded.id);
    res.set('X-User-Role', decoded.role);
    res.status(200).send('OK');
  } catch (e) {
    res.status(401).json({ error: 'Invalid token' });
  }
});

app.listen(8086, () => console.log('Auth service running on 8086'));
