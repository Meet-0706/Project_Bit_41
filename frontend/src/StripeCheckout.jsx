import React, { useState } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import axios from 'axios';

const StripeCheckout = ({ amount, onSuccess, onCancel }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setProcessing(true);
    setError(null);

    if (!stripe || !elements) {
      setProcessing(false);
      return;
    }

    try {
      // 1. Create PaymentIntent on the backend
      const res = await axios.post('/api/payment/create-intent', { amount });
      const clientSecret = res.data.clientSecret;

      // 2. Confirm the payment on the frontend
      const payload = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: elements.getElement(CardElement),
        }
      });

      if (payload.error) {
        setError(`Payment failed: ${payload.error.message}`);
        setProcessing(false);
      } else {
        setError(null);
        setProcessing(false);
        onSuccess(payload.paymentIntent.id);
      }
    } catch (err) {
      console.error(err);
      setError("An error occurred during payment processing.");
      setProcessing(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="stripe-checkout-form" style={{marginTop: '20px', background: '#f8f9fa', padding: '15px', borderRadius: '4px'}}>
      <h4 style={{marginBottom: '15px', fontSize: '1rem'}}>Enter Card Details (Test Mode)</h4>
      <div style={{padding: '10px', background: 'white', border: '1px solid #ccc', borderRadius: '4px', marginBottom: '15px'}}>
        <CardElement options={{hidePostalCode: true, style: {base: {fontSize: '16px', color: '#424770', '::placeholder': {color: '#aab7c4'}}}}} />
      </div>
      {error && <div style={{color: 'red', marginBottom: '15px', fontSize: '14px'}}>{error}</div>}
      <div style={{display: 'flex', gap: '10px'}}>
        <button type="button" onClick={onCancel} className="btn btn-secondary" style={{flex: 1}} disabled={processing}>Cancel</button>
        <button type="submit" className="btn btn-primary" style={{flex: 1}} disabled={!stripe || processing}>
          {processing ? 'Processing...' : `Pay ₹${amount}`}
        </button>
      </div>
    </form>
  );
};

export default StripeCheckout;
