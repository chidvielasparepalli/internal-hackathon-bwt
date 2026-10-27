import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

// Pure domain logic functions matching HarvestLink specifications

export function validateQuantity(quantity, availableStock, minOrderQty) {
  if (typeof quantity !== 'number' || isNaN(quantity) || !isFinite(quantity)) {
    return { valid: false, error: 'Please enter a valid numeric quantity' };
  }
  if (quantity <= 0) {
    return { valid: false, error: 'Quantity must be greater than zero' };
  }
  if (quantity > availableStock) {
    return { valid: false, error: `Requested quantity exceeds available stock of ${availableStock}` };
  }
  if (quantity < minOrderQty) {
    return { valid: false, error: `Minimum order quantity is ${minOrderQty}` };
  }
  return { valid: true };
}

export function calculateOrderTotal(items) {
  return items.reduce((sum, item) => sum + (item.quantity * item.unitPriceSnapshot), 0);
}

export function applyOrderStockReservation(product, quantityToReserve) {
  if (quantityToReserve <= 0) throw new Error('Reservation quantity must be positive');
  if (product.quantityAvailable < quantityToReserve) {
    throw new Error(`Insufficient stock for ${product.name}`);
  }
  return {
    ...product,
    quantityAvailable: product.quantityAvailable - quantityToReserve
  };
}

export function applyStockRestoration(product, quantityToRestore) {
  if (quantityToRestore <= 0) throw new Error('Restoration quantity must be positive');
  return {
    ...product,
    quantityAvailable: product.quantityAvailable + quantityToRestore
  };
}

export function canTransitionOrderStatus(currentStatus, targetStatus, actorRole) {
  if (currentStatus === targetStatus) return false;

  switch (currentStatus) {
    case 'requested':
      if (targetStatus === 'accepted') return actorRole === 'farmer' || actorRole === 'admin';
      if (targetStatus === 'rejected') return actorRole === 'farmer' || actorRole === 'admin';
      if (targetStatus === 'cancelled') return actorRole === 'buyer' || actorRole === 'admin';
      return false;
    case 'accepted':
      if (targetStatus === 'ready') return actorRole === 'farmer' || actorRole === 'admin';
      if (targetStatus === 'rejected') return actorRole === 'farmer' || actorRole === 'admin';
      if (targetStatus === 'cancelled') return actorRole === 'admin';
      return false;
    case 'ready':
      if (targetStatus === 'completed') return actorRole === 'farmer' || actorRole === 'admin';
      return false; // Cancellation after ready is NEVER allowed
    case 'completed':
    case 'cancelled':
    case 'rejected':
      return false;
    default:
      return false;
  }
}

export function simulateConcurrentOrders(initialProduct, orderRequests) {
  let currentProduct = { ...initialProduct };
  const successfulOrders = [];

  for (const { buyerId, requestedQty } of orderRequests) {
    const validation = validateQuantity(requestedQty, currentProduct.quantityAvailable, currentProduct.minOrderQty);
    if (validation.valid) {
      currentProduct = applyOrderStockReservation(currentProduct, requestedQty);
      successfulOrders.push({ buyerId, requestedQty });
    }
  }
  return { successfulOrders, finalProduct: currentProduct };
}

// ----------------- UPGRADE PURE LOGIC HELPERS -----------------

export function isValidIndianPhone(phone) {
  const clean = String(phone).replace(/[^0-9]/g, '').slice(-10);
  return /^[6-9]\d{9}$/.test(clean);
}

export function validateSignUp(email, phone, password, confirmPassword) {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    return { valid: false, error: 'Please enter a valid email address' };
  }
  if (!isValidIndianPhone(phone)) {
    return { valid: false, error: 'Please enter a valid 10-digit Indian phone number starting with 6-9' };
  }
  if (password.length < 8) {
    return { valid: false, error: 'Password must be at least 8 characters long' };
  }
  if (password !== confirmPassword) {
    return { valid: false, error: 'Passwords do not match' };
  }
  return { valid: true };
}

export function determinePaymentStatus(method, simulateSuccess = true) {
  if (method === 'CASH_ON_PICKUP') return 'CASH_DUE';
  return simulateSuccess ? 'PAID_SIMULATED' : 'FAILED';
}

export function resolveRefundOnCancellation(currentPaymentStatus) {
  return currentPaymentStatus === 'PAID_SIMULATED' ? 'REFUND_SIMULATED' : currentPaymentStatus;
}

export function validateReviewSubmission(order, rating, text, existingReviewsForOrder = []) {
  if (order.status !== 'completed') {
    return { valid: false, error: 'Reviews can only be submitted for Completed orders' };
  }
  if (existingReviewsForOrder.some(r => r.orderId === order.id)) {
    return { valid: false, error: 'Only one review is permitted per completed order' };
  }
  if (rating < 1 || rating > 5) {
    return { valid: false, error: 'Rating must be between 1 and 5 stars' };
  }
  if (text.length > 300) {
    return { valid: false, error: 'Review cannot exceed 300 characters' };
  }
  return { valid: true };
}

export function calculateAverageRating(reviews) {
  const visible = reviews.filter(r => !r.hidden);
  if (visible.length === 0) return 0.0;
  const sum = visible.reduce((acc, r) => acc + r.rating, 0);
  return Math.round((sum / visible.length) * 10) / 10;
}

// ----------------- TEST SUITE -----------------

describe('HarvestLink Stock & Order Pure Logic Tests', () => {
  const sampleProduct = {
    id: 'prod-1',
    name: 'Guntur Sannam Red Chillies',
    quantityAvailable: 50.0,
    pricePerUnit: 220.0,
    minOrderQty: 5.0
  };

  // Original 11 tests
  it('rejects zero quantity', () => {
    const res = validateQuantity(0, 50, 5);
    assert.equal(res.valid, false);
    assert.equal(res.error, 'Quantity must be greater than zero');
  });

  it('rejects negative quantity', () => {
    const res = validateQuantity(-5, 50, 5);
    assert.equal(res.valid, false);
    assert.equal(res.error, 'Quantity must be greater than zero');
  });

  it('rejects over-stock quantity', () => {
    const res = validateQuantity(60, 50, 5);
    assert.equal(res.valid, false);
    assert.ok(res.error.includes('exceeds available stock'));
  });

  it('respects minimum order quantity', () => {
    const res = validateQuantity(3, 50, 5);
    assert.equal(res.valid, false);
    assert.equal(res.error, 'Minimum order quantity is 5');
  });

  it('accepts valid quantity', () => {
    const res = validateQuantity(10, 50, 5);
    assert.equal(res.valid, true);
  });

  it('calculates order total correctly', () => {
    const items = [
      { quantity: 10, unitPriceSnapshot: 28 },
      { quantity: 5, unitPriceSnapshot: 40 }
    ];
    assert.equal(calculateOrderTotal(items), 480);
  });

  it('decrements stock on order reservation', () => {
    const updated = applyOrderStockReservation(sampleProduct, 20);
    assert.equal(updated.quantityAvailable, 30);
  });

  it('restores stock on order cancel or rejection', () => {
    const reserved = applyOrderStockReservation(sampleProduct, 20);
    assert.equal(reserved.quantityAvailable, 30);
    const restored = applyStockRestoration(reserved, 20);
    assert.equal(restored.quantityAvailable, 50);
  });

  it('blocks invalid status transitions', () => {
    assert.equal(canTransitionOrderStatus('requested', 'completed', 'farmer'), false);
    assert.equal(canTransitionOrderStatus('requested', 'accepted', 'buyer'), false);
    assert.equal(canTransitionOrderStatus('requested', 'ready', 'farmer'), false);
  });

  it('blocks cancellation after order is ready', () => {
    assert.equal(canTransitionOrderStatus('ready', 'cancelled', 'buyer'), false);
    assert.equal(canTransitionOrderStatus('ready', 'cancelled', 'farmer'), false);
    assert.equal(canTransitionOrderStatus('ready', 'cancelled', 'admin'), false);
  });

  it('simulates concurrent competing orders where only one succeeds on limited stock', () => {
    const requests = [
      { buyerId: 'buyer-alpha', requestedQty: 35 },
      { buyerId: 'buyer-beta', requestedQty: 35 }
    ];
    const { successfulOrders, finalProduct } = simulateConcurrentOrders(sampleProduct, requests);
    assert.equal(successfulOrders.length, 1);
    assert.equal(successfulOrders[0].buyerId, 'buyer-alpha');
    assert.equal(finalProduct.quantityAvailable, 15);
  });

  // Extended Upgrade Tests
  it('validates sign-up rules (email, 10-digit Indian phone, password min 8)', () => {
    const valid = validateSignUp('buyer@hyderabad.in', '9848012345', 'Password123!', 'Password123!');
    assert.equal(valid.valid, true);

    const badPhone = validateSignUp('buyer@hyderabad.in', '12345', 'Password123!', 'Password123!');
    assert.equal(badPhone.valid, false);

    const shortPass = validateSignUp('buyer@hyderabad.in', '9848012345', 'short', 'short');
    assert.equal(shortPass.valid, false);
  });

  it('handles simulated payment transitions and refund simulation', () => {
    assert.equal(determinePaymentStatus('CASH_ON_PICKUP'), 'CASH_DUE');
    assert.equal(determinePaymentStatus('UPI_SIMULATED', true), 'PAID_SIMULATED');
    assert.equal(determinePaymentStatus('UPI_SIMULATED', false), 'FAILED');
    assert.equal(resolveRefundOnCancellation('PAID_SIMULATED'), 'REFUND_SIMULATED');
    assert.equal(resolveRefundOnCancellation('CASH_DUE'), 'CASH_DUE');
  });

  it('enforces one review per completed order and blocks before completion', () => {
    const completedOrder = { id: 'ord-1', status: 'completed' };
    const pendingOrder = { id: 'ord-2', status: 'requested' };

    // Block before completion
    const early = validateReviewSubmission(pendingOrder, 5, 'Great');
    assert.equal(early.valid, false);

    // Allow on completed
    const valid = validateReviewSubmission(completedOrder, 5, 'Great');
    assert.equal(valid.valid, true);

    // Block duplicate review
    const duplicate = validateReviewSubmission(completedOrder, 4, 'Again', [{ orderId: 'ord-1' }]);
    assert.equal(duplicate.valid, false);
  });

  it('calculates average rating excluding hidden reviews', () => {
    const reviews = [
      { rating: 5, hidden: false },
      { rating: 4, hidden: false },
      { rating: 4, hidden: false },
      { rating: 1, hidden: true } // Spam / hidden
    ];
    assert.equal(calculateAverageRating(reviews), 4.3);
  });
});
