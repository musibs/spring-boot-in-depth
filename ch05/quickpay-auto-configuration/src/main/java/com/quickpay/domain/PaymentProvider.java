package com.quickpay.domain;

/**
 * Enum representing supported payment providers in QuickPay orchestration system.
 * Each provider has unique integration requirements and API characteristics.
 */
public enum PaymentProvider {

    /**
     * Stripe payment provider - supports card payments, ACH, and digital wallets
     */
    STRIPE("stripe", "https://api.stripe.com"),

    /**
     * PayPal payment provider - supports PayPal accounts and card payments
     */
    PAYPAL("paypal", "https://api.paypal.com"),

    /**
     * Square payment provider - point-of-sale and online payments
     */
    SQUARE("square", "https://connect.squareup.com"),

    /**
     * Adyen payment provider - global payment platform
     */
    ADYEN("adyen", "https://checkout-test.adyen.com");

    private final String providerId;
    private final String defaultBaseUrl;

    PaymentProvider(String providerId, String defaultBaseUrl) {
        this.providerId = providerId;
        this.defaultBaseUrl = defaultBaseUrl;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }
}
