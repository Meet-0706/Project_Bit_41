import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from './App';
import './Storefront.css';
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';
import StripeCheckout from './StripeCheckout';

const stripePromise = loadStripe('pk_test_51UO1nlREg117qM2JBv2WgvJTcoYTBEtNcsjcwRmvlMFGZ21G64o7bnYVoLfypdZRNR8LwWdue5Cwyvbemm7xMOF900oEfhm1TG');


const CATEGORIES = ["All", "Audio", "Wearables", "Power", "Cameras", "Accessories", "Home"];
const CATEGORY_ICONS = {
  Audio: "🎧", Wearables: "⌚", Power: "🔋", Cameras: "📷", Accessories: "🖱️", Home: "💡", All: "✨"
};
const CATEGORY_COLORS = {
  Audio: "#e7ecff", Wearables: "#ffe9e0", Power: "#fff3d6", Cameras: "#e2f5ea", Accessories: "#f0e8ff", Home: "#fff0f4"
};

const Storefront = () => {
  const { user, logout } = useContext(AuthContext);
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState({});
  const [orders, setOrders] = useState([]);
  const [activeCategory, setActiveCategory] = useState("All");
  
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);
  const [orderConfirmed, setOrderConfirmed] = useState(null);
  const [isPlacingOrder, setIsPlacingOrder] = useState(false);
  const [showOrders, setShowOrders] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState('prepaid');
  const [showStripe, setShowStripe] = useState(false);

  useEffect(() => {
    fetchProducts();
    fetchOrders();
    const interval = setInterval(fetchOrders, 3000);
    return () => clearInterval(interval);
  }, []);

  const fetchProducts = async () => {
    try {
      const res = await axios.get('/api/products');
      const enriched = res.data.map((p, i) => ({
        ...p,
        category: p.category || "Accessories",
        rating: 4.0 + (i % 10) / 10,
        ratingCount: 100 + i * 45,
        mrp: p.price * 1.3
      }));
      setProducts(enriched);
    } catch (e) {
      console.error("Error fetching products", e);
    }
  };

  const fetchOrders = async () => {
    try {
      const res = await axios.get('/api/orders');
      setOrders(res.data);
    } catch (e) {
      console.error("Error fetching orders", e);
    }
  };

  const visibleProducts = activeCategory === "All" 
    ? products 
    : products.filter(p => p.category === activeCategory);

  const changeQty = (id, delta) => {
    setCart(prev => {
      const current = prev[id] || 0;
      const next = Math.max(0, current + delta);
      const newCart = { ...prev, [id]: next };
      if (next === 0) delete newCart[id];
      return newCart;
    });
  };

  const cartEntries = Object.entries(cart).map(([id, qty]) => ({
    product: products.find(p => p.id === id),
    qty
  })).filter(e => e.product);

  const cartSubtotal = cartEntries.reduce((sum, { product, qty }) => sum + product.price * qty, 0);
  const cartCount = cartEntries.reduce((sum, e) => sum + e.qty, 0);

  const money = (n) => `₹${Number(n).toLocaleString("en-IN", { minimumFractionDigits: 2 })}`;
  
  const discountPct = (p) => {
    if (!p.mrp || p.mrp <= p.price) return 0;
    return Math.round(((p.mrp - p.price) / p.mrp) * 100);
  };

  const handleCheckoutSubmit = async (e) => {
    e.preventDefault();
    if (paymentMethod === 'prepaid' && !showStripe) {
      setShowStripe(true);
      return;
    }
    await placeBackendOrder();
  };

  const placeBackendOrder = async () => {
    setIsPlacingOrder(true);
    try {
      const items = cartEntries.map(e => ({
        productId: e.product.id,
        quantity: e.qty,
        price: e.product.price
      }));
      
      const res = await axios.post('/api/orders', {
        customerId: user.id,
        totalAmount: cartSubtotal,
        items
      });
      
      setCart({});
      setOrderConfirmed({
        orderId: res.data.id,
        status: res.data.status,
        shipmentId: `SHIP-${Date.now()}`
      });
      setShowStripe(false);
      fetchOrders();
    } catch (err) {
      alert("Checkout failed. Please try again.");
    } finally {
      setIsPlacingOrder(false);
    }
  };

  return (
    <div className="nexbyte-theme">
      <header className="topbar">
        <div className="wrap topbar-row">
          <a className="logo" href="#" onClick={(e) => {e.preventDefault(); setShowOrders(false);}}>
            Nexbyte<span className="logo-dot">.</span>
            <em>Explore <strong>Plus</strong> ⚡</em>
          </a>

          <div className="search-box">
            <input type="text" placeholder="Search for gadgets, brands and more" />
            <button className="search-btn" aria-label="Search">🔍</button>
          </div>

          <nav className="topbar-actions">
            <button className="login-btn" onClick={logout}>Logout</button>
            <button className="more-link" style={{background:'none',border:'none',color:'white'}} onClick={() => setShowOrders(!showOrders)}>
              {showOrders ? '▾ Catalog' : '▾ My Orders'}
            </button>
            <button className="cart-btn" onClick={() => setIsCartOpen(true)}>
              🛒 Cart <span className="cart-count">{cartCount}</span>
            </button>
          </nav>
        </div>
      </header>

      {!showOrders ? (
        <>
          <nav className="category-strip">
            <div className="wrap category-row">
              {CATEGORIES.map(cat => (
                <button 
                  key={cat}
                  className={`category-chip ${activeCategory === cat ? 'active' : ''}`}
                  onClick={() => setActiveCategory(cat)}
                >
                  <span className="cat-emoji">{CATEGORY_ICONS[cat]}</span>
                  {cat}
                </button>
              ))}
            </div>
          </nav>

          <main className="wrap">
            <section className="banner">
              <div className="banner-slide">
                <span className="banner-eyebrow">BIG GADGET DAYS</span>
                <h1>Up to 40% off on audio &amp; wearables</h1>
                <p>Free delivery · Auto-shipped within hours of ordering</p>
              </div>
            </section>

            <section className="delivery-strip">
              <span>🚚 Free delivery on orders above ₹499</span>
              <span>⚡ Fast dispatch within 24 hours</span>
              <span>↩️ 7-day easy returns</span>
            </section>

            <section className="catalog">
              <h2>Recommended for you</h2>
              <div className="product-grid">
                {visibleProducts.map(p => {
                  const qty = cart[p.id] || 0;
                  const off = discountPct(p);
                  return (
                    <article className="product-card" key={p.id}>
                      <div className="product-thumb" style={{background: p.imageUrl ? "transparent" : (CATEGORY_COLORS[p.category] || "#eef1f6")}}>
                        {p.imageUrl ? (
                          <img src={p.imageUrl} alt={p.name} style={{width: '100%', height: '100%', objectFit: 'cover', borderRadius: '4px'}} />
                        ) : (
                          CATEGORY_ICONS[p.category] || "🔌"
                        )}
                      </div>
                      <span className="product-category">{p.category}</span>
                      <h3>{p.name}</h3>
                      <p className="product-desc">{p.description}</p>
                      <div className="rating-row">
                        <span className="rating-badge">{p.rating.toFixed(1)} ★</span>
                        <span className="rating-count">{p.ratingCount.toLocaleString("en-IN")} ratings</span>
                      </div>
                      <div className="price-row">
                        <span className="price-now">{money(p.price)}</span>
                        {p.mrp && <span className="price-mrp">{money(p.mrp)}</span>}
                        {off > 0 && <span className="price-off">{off}% off</span>}
                      </div>
                      <div className="product-row">
                        {qty === 0 ? (
                          <button className="add-btn" onClick={() => changeQty(p.id, 1)}>ADD TO CART</button>
                        ) : (
                          <div className="qty-stepper">
                            <button onClick={() => changeQty(p.id, -1)}>{'-'}</button>
                            <span>{qty}</span>
                            <button onClick={() => changeQty(p.id, 1)}>+</button>
                          </div>
                        )}
                      </div>
                    </article>
                  );
                })}
              </div>
            </section>
          </main>
        </>
      ) : (
        <main className="wrap" style={{paddingTop: '30px', minHeight: '60vh'}}>
          <h2 style={{fontSize: '1.5rem', marginBottom: '20px'}}>My Orders</h2>
          <div className="orders-list">
            {orders.slice().reverse().map(order => (
              <div key={order.id} className="order-card">
                <div>
                  <div className="order-id">Order #{order.id.split('-')[0]}</div>
                  <div style={{fontSize: '0.8rem', color: 'var(--muted)'}}>{new Date(order.createdAt).toLocaleString()}</div>
                  <div style={{marginTop: '8px', fontWeight: '500'}}>{money(order.totalAmount)}</div>
                </div>
                <div>
                  <span className={`order-status status-${order.status}`}>{order.status}</span>
                </div>
              </div>
            ))}
            {orders.length === 0 && (
              <div style={{padding: '40px', textAlign: 'center', background: '#fff', border: '1px solid var(--border)', borderRadius: '3px', color: 'var(--muted)'}}>
                You haven't placed any orders yet.
              </div>
            )}
          </div>
        </main>
      )}

      <footer className="site-footer">
        <div className="wrap footer-grid">
          <div>
            <h4>ABOUT</h4>
            <span>Nexbyte Gadgets</span>
          </div>
          <div>
            <h4>HELP</h4>
            <span>Track your order</span>
            <span>Returns</span>
          </div>
          <div>
            <h4>SUPPORT</h4>
            <span>24/7 Dedicated Support</span>
          </div>
        </div>
        <div className="wrap footer-bottom">© Nexbyte — Modern E-Commerce Platform</div>
      </footer>

      {/* Cart Drawer */}
      <div className={`drawer-overlay ${isCartOpen ? 'open' : ''}`} onClick={() => setIsCartOpen(false)}></div>
      <aside className={`cart-drawer ${isCartOpen ? 'open' : ''}`}>
        <div className="drawer-head">
          <h3>My Cart</h3>
          <button className="icon-btn" onClick={() => setIsCartOpen(false)}>×</button>
        </div>
        <div className="cart-lines">
          {cartEntries.length > 0 ? cartEntries.map(({product, qty}) => (
            <div className="cart-line" key={product.id}>
              <div>
                <div className="cart-line-name">{product.name}</div>
                <div className="cart-line-sku">{money(product.price)} each</div>
              </div>
              <div className="qty-stepper">
                <button onClick={() => changeQty(product.id, -1)}>{'-'}</button>
                <span>{qty}</span>
                <button onClick={() => changeQty(product.id, 1)}>+</button>
              </div>
            </div>
          )) : (
            <p className="cart-empty">Your cart is empty.</p>
          )}
        </div>
        <div className="cart-footer">
          <div className="cart-subtotal">
            <span>Order Total</span>
            <span className="price-now">{money(cartSubtotal)}</span>
          </div>
          <button 
            className="btn btn-primary btn-full" 
            disabled={cartEntries.length === 0}
            onClick={() => { setIsCartOpen(false); setIsCheckoutOpen(true); }}
          >
            PLACE ORDER
          </button>
        </div>
      </aside>

      {/* Checkout Modal */}
      <div className={`modal-overlay ${isCheckoutOpen ? 'open' : ''}`}>
        <div className="modal">
          <div className="modal-head">
            <h3>{orderConfirmed ? 'Order Summary' : 'Delivery Address'}</h3>
            <button className="icon-btn" onClick={() => { setIsCheckoutOpen(false); setOrderConfirmed(null); }}>×</button>
          </div>
          
          {!orderConfirmed ? (
            showStripe ? (
              <Elements stripe={stripePromise}>
                <StripeCheckout 
                  amount={cartSubtotal} 
                  onSuccess={() => placeBackendOrder()}
                  onCancel={() => setShowStripe(false)}
                />
              </Elements>
            ) : (
            <form className="checkout-form" onSubmit={handleCheckoutSubmit}>
              <div className="field-row">
                <label>Full name<input type="text" required /></label>
                <label>Phone<input type="tel" required pattern="[0-9]{10}" placeholder="10-digit mobile" /></label>
              </div>
              <label>Email<input type="email" required defaultValue={user.email}/></label>
              <label>Address<input type="text" required /></label>
              <div className="field-row">
                <label>City<input type="text" required /></label>
                <label>State<input type="text" required /></label>
                <label>Pincode<input type="text" required pattern="[0-9]{6}" /></label>
              </div>

              <fieldset className="payment-choice">
                <legend>Payment method</legend>
                <label><input type="radio" name="pm" value="prepaid" checked={paymentMethod === 'prepaid'} onChange={() => setPaymentMethod('prepaid')} /> Prepaid (Cards/UPI)</label>
                <label><input type="radio" name="pm" value="cod" checked={paymentMethod === 'cod'} onChange={() => setPaymentMethod('cod')} /> Cash on Delivery</label>
              </fieldset>

              <div className="manifest-summary">
                <span>Order Total</span>
                <span className="price-now">{money(cartSubtotal)}</span>
              </div>

              <button type="submit" className="btn btn-primary btn-full" disabled={isPlacingOrder}>
                {paymentMethod === 'prepaid' ? 'PROCEED TO PAYMENT' : (isPlacingOrder ? 'PLACING ORDER...' : 'CONFIRM ORDER')}
              </button>
            </form>
            )
          ) : (
            <div className="confirmation">
              <div className="confirmation-icon">✓</div>
              <h4>Order placed</h4>
              <dl className="manifest-dl">
                <dt>Order ID</dt><dd>{orderConfirmed.orderId}</dd>
                <dt>Status</dt><dd>{orderConfirmed.status}</dd>
                <dt>Shipment ID</dt><dd>{orderConfirmed.shipmentId}</dd>
              </dl>
              <p className="confirmation-note">Order confirmed and packed for delivery.</p>
              <button className="btn btn-secondary btn-full" onClick={() => { setIsCheckoutOpen(false); setOrderConfirmed(null); }}>
                Continue Shopping
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Storefront;
