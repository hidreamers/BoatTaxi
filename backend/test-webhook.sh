# Webhook Test Script
# Run this to test webhook functionality

echo "Testing webhook endpoint through ngrok..."

# Test with ngrok URL
curl -X POST https://unluxuriant-geyseric-genia.ngrok-free.dev/api/webhooks \
  -H "Content-Type: application/json" \
  -H "Stripe-Signature: t=1234567890,v1=test_signature" \
  -d '{
    "id": "evt_test_webhook",
    "object": "event",
    "api_version": "2020-08-27",
    "created": 1234567890,
    "data": {
      "object": {
        "id": "cs_test_123",
        "object": "checkout.session",
        "amount_total": 199,
        "currency": "usd",
        "client_reference_id": "test_user_123",
        "metadata": {
          "planId": "DAY_PASS"
        },
        "payment_intent": "pi_test_123",
        "payment_status": "paid"
      }
    },
    "livemode": false,
    "pending_webhooks": 1,
    "request": {
      "id": "req_test_123",
      "idempotency_key": null
    },
    "type": "checkout.session.completed"
  }'

echo -e "\n\nTest completed. Check server logs for webhook processing."
      "object": {
        "id": "cs_test_123",
        "object": "checkout.session",
        "amount_total": 199,
        "currency": "usd",
        "metadata": {
          "userId": "test_user_123",
          "planId": "DAY_PASS"
        },
        "payment_intent": "pi_test_123",
        "payment_status": "paid"
      }
    },
    "livemode": false,
    "pending_webhooks": 1,
    "request": {
      "id": "req_test_123",
      "idempotency_key": null
    },
    "type": "checkout.session.completed"
  }'

echo -e "\n\nWebhook test completed. Check server logs for processing details."