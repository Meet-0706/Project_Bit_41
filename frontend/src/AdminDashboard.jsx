import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { Package, ShoppingCart, Activity, AlertCircle, LogOut } from 'lucide-react';
import { AuthContext } from './App';

const AdminDashboard = () => {
  const [products, setProducts] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [orders, setOrders] = useState([]);
  const [failureRate, setFailureRate] = useState(0.2);
  const [notifications, setNotifications] = useState([]);
  const { logout } = useContext(AuthContext);

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 5000); // refresh every 5s
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    try {
      const [prodRes, invRes, ordRes, payRes, notifRes] = await Promise.all([
        axios.get('/api/products').catch(() => ({ data: [] })),
        axios.get('/api/inventory').catch(() => ({ data: [] })),
        axios.get('/api/orders').catch(() => ({ data: [] })),
        axios.get('/api/payment/config').catch(() => ({ data: { failureRate: 0.2 } })),
        axios.get('/api/notifications').catch(() => ({ data: [] }))
      ]);
      setProducts(prodRes.data);
      setInventory(invRes.data);
      setOrders(ordRes.data);
      setFailureRate(payRes.data.failureRate || 0);
      setNotifications(notifRes.data);
    } catch (e) {
      console.error("Error fetching data", e);
    }
  };

  const updateFailureRate = async (newRate) => {
    await axios.put('/api/payment/config', { failureRate: parseFloat(newRate) });
    setFailureRate(newRate);
  };

  return (
    <div className="min-h-screen bg-gray-100 p-8 font-sans text-gray-900">
      <header className="mb-8 flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-800 flex items-center gap-2">
          <Activity className="text-blue-600" />
          Store Management & Admin Dashboard
        </h1>
        <div className="flex gap-4 items-center">
          <div className="bg-white p-2 rounded shadow flex items-center gap-2">
            <label className="text-sm font-semibold">Payment Failure Rate:</label>
            <input 
              type="range" min="0" max="1" step="0.1" 
              value={failureRate} 
              onChange={(e) => updateFailureRate(e.target.value)}
            />
            <span className="font-mono bg-gray-200 px-2 py-1 rounded">{(failureRate * 100).toFixed(0)}%</span>
          </div>
          <button 
            onClick={logout}
            className="flex items-center gap-1 bg-red-600 text-white px-4 py-2 rounded shadow hover:bg-red-700 transition"
          >
            <LogOut size={16}/> Logout
          </button>
        </div>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Products & Inventory */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 border-b pb-2">
            <Package className="text-green-600"/> Inventory Status
          </h2>
          <div className="space-y-3">
            {products.map(p => {
              const inv = inventory.find(i => i.productId === p.id) || { availableQuantity: 0, reservedQuantity: 0 };
              return (
                <div key={p.id} className="p-3 bg-gray-50 rounded border">
                  <div className="font-semibold">{p.name} <span className="text-xs text-gray-500">({p.sku})</span></div>
                  <div className="text-sm mt-1 flex gap-4">
                    <span className="text-green-700 font-medium">Available: {inv.availableQuantity}</span>
                    <span className="text-orange-600 font-medium">Reserved: {inv.reservedQuantity}</span>
                  </div>
                </div>
              );
            })}
            {products.length === 0 && <div className="text-gray-500 text-sm italic">No products found.</div>}
          </div>
        </div>

        {/* Orders */}
        <div className="bg-white p-6 rounded-lg shadow-md col-span-1 lg:col-span-2">
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 border-b pb-2">
            <ShoppingCart className="text-purple-600"/> All Orders
          </h2>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="bg-gray-100">
                  <th className="p-2 rounded-tl">Order ID</th>
                  <th className="p-2">Status</th>
                  <th className="p-2">Total</th>
                  <th className="p-2 rounded-tr">Time</th>
                </tr>
              </thead>
              <tbody>
                {orders.slice().reverse().map(o => (
                  <tr key={o.id} className="border-b">
                    <td className="p-2 font-mono text-xs">{o.id.split('-')[0]}...</td>
                    <td className="p-2">
                      <span className={`px-2 py-1 rounded text-xs font-bold ${
                        o.status === 'CONFIRMED' ? 'bg-green-100 text-green-800' :
                        o.status === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                        'bg-yellow-100 text-yellow-800'
                      }`}>
                        {o.status}
                      </span>
                    </td>
                    <td className="p-2">${o.totalAmount}</td>
                    <td className="p-2 text-gray-500">{new Date(o.createdAt).toLocaleTimeString()}</td>
                  </tr>
                ))}
                {orders.length === 0 && <tr><td colSpan="4" className="p-4 text-center text-gray-500 italic">No orders yet.</td></tr>}
              </tbody>
            </table>
          </div>
        </div>

        {/* Notifications / Event Log */}
        <div className="bg-white p-6 rounded-lg shadow-md col-span-1 md:col-span-2 lg:col-span-3">
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 border-b pb-2">
            <AlertCircle className="text-blue-600"/> Notification Logs
          </h2>
          <div className="bg-gray-900 text-green-400 p-4 rounded font-mono text-xs h-64 overflow-y-auto space-y-2">
            {notifications.slice().reverse().map(n => (
              <div key={n.id} className="border-b border-gray-700 pb-1">
                <span className="text-gray-500">[{new Date(n.sentAt).toISOString()}]</span> 
                {' '}Sent <strong className={n.type === 'ORDER_CANCELLED' ? 'text-red-400' : 'text-green-400'}>{n.type}</strong> 
                {' '}for Order {n.orderId} (Status: {n.status})
              </div>
            ))}
            {notifications.length === 0 && <div className="text-gray-500">Waiting for events...</div>}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
