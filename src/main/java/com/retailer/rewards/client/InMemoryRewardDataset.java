package com.retailer.rewards.client;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Purchase;
import com.retailer.rewards.model.PurchaseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Sample retailer history for July through October 2026, plus one June purchase that must
 * stay outside a July-September window. The records stand in for a downstream transaction API.
 */
@Component
public class InMemoryRewardDataset {

    private final List<Customer> customers;
    private final Map<String, Customer> customersById;
    private final Map<String, List<Purchase>> purchasesByCustomerId;

    public InMemoryRewardDataset() {
        List<Customer> loadedCustomers = new ArrayList<Customer>();
        loadedCustomers.add(new Customer("C1001", "Maya", "Chen", "maya.chen@example.com", LocalDate.parse("2024-03-15")));
        loadedCustomers.add(new Customer("C1002", "Luis", "Ortega", "luis.ortega@example.com", LocalDate.parse("2025-01-09")));
        loadedCustomers.add(new Customer("C1003", "Priya", "Nair", "priya.nair@example.com", LocalDate.parse("2023-11-02")));
        loadedCustomers.add(new Customer("C1004", "Jonah", "Blake", "jonah.blake@example.com", LocalDate.parse("2026-06-01")));
        loadedCustomers.add(new Customer("C1005", "Elena", "Rossi", "elena.rossi@example.com", LocalDate.parse("2022-08-19")));

        List<Purchase> loadedPurchases = new ArrayList<Purchase>();
        addPurchase(loadedPurchases, "T10011", "C1001", "2026-07-04", "120.00", PurchaseCategory.ELECTRONICS, "Wireless headphones");
        addPurchase(loadedPurchases, "T10012", "C1001", "2026-07-18", "75.50", PurchaseCategory.GROCERY, "Weekly groceries");
        addPurchase(loadedPurchases, "T10013", "C1001", "2026-08-02", "49.99", PurchaseCategory.PHARMACY, "Pharmacy pickup");
        addPurchase(loadedPurchases, "T10014", "C1001", "2026-08-20", "100.00", PurchaseCategory.HOME, "Cookware set");
        addPurchase(loadedPurchases, "T10015", "C1001", "2026-09-11", "200.00", PurchaseCategory.ELECTRONICS, "Tablet");
        addPurchase(loadedPurchases, "T10016", "C1001", "2026-10-05", "60.00", PurchaseCategory.GROCERY, "Grocery run");

        addPurchase(loadedPurchases, "T10021", "C1002", "2026-07-09", "40.00", PurchaseCategory.GROCERY, "Snacks");
        addPurchase(loadedPurchases, "T10022", "C1002", "2026-08-14", "250.75", PurchaseCategory.ELECTRONICS, "Speaker system");
        addPurchase(loadedPurchases, "T10023", "C1002", "2026-09-28", "50.00", PurchaseCategory.APPAREL, "Socks");
        addPurchase(loadedPurchases, "T10024", "C1002", "2026-10-02", "101.99", PurchaseCategory.HOME, "Table lamp");

        addPurchase(loadedPurchases, "T10031", "C1003", "2026-07-01", "50.00", PurchaseCategory.GROCERY, "Produce box");
        addPurchase(loadedPurchases, "T10032", "C1003", "2026-08-15", "50.01", PurchaseCategory.GROCERY, "Bakery order");
        addPurchase(loadedPurchases, "T10033", "C1003", "2026-09-01", "100.99", PurchaseCategory.HOME, "Bedding");
        addPurchase(loadedPurchases, "T10034", "C1003", "2026-10-08", "101.00", PurchaseCategory.APPAREL, "Jacket");

        addPurchase(loadedPurchases, "T10041", "C1004", "2026-08-22", "80.00", PurchaseCategory.GROCERY, "Dinner party supplies");
        addPurchase(loadedPurchases, "T10042", "C1004", "2026-09-30", "65.00", PurchaseCategory.GROCERY, "Month-end groceries");
        addPurchase(loadedPurchases, "T10043", "C1004", "2026-10-01", "30.00", PurchaseCategory.PHARMACY, "Vitamins");

        addPurchase(loadedPurchases, "T10051", "C1005", "2026-06-15", "500.00", PurchaseCategory.ELECTRONICS, "Camera body");
        addPurchase(loadedPurchases, "T10052", "C1005", "2026-09-19", "120.00", PurchaseCategory.APPAREL, "Wool coat");
        addPurchase(loadedPurchases, "T10053", "C1005", "2026-10-11", "45.00", PurchaseCategory.GROCERY, "Produce");

        this.customersById = indexCustomers(loadedCustomers);
        this.customers = immutableCustomers(loadedCustomers);
        this.purchasesByCustomerId = indexPurchases(loadedCustomers, loadedPurchases);
    }

    public Optional<Customer> findCustomer(String customerId) {
        return Optional.ofNullable(customersById.get(customerId));
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Purchase> findPurchases(String customerId, LocalDate startDate, LocalDate endDate) {
        List<Purchase> customerPurchases = purchasesByCustomerId.get(customerId);
        if (customerPurchases == null || customerPurchases.isEmpty()) {
            return Collections.emptyList();
        }
        List<Purchase> matches = new ArrayList<Purchase>();
        for (Purchase purchase : customerPurchases) {
            LocalDate transactionDate = purchase.getTransactionDate();
            if (!transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate)) {
                matches.add(purchase);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    private static void addPurchase(
            List<Purchase> purchases,
            String transactionId,
            String customerId,
            String transactionDate,
            String amount,
            PurchaseCategory category,
            String description) {
        purchases.add(new Purchase(
                transactionId,
                customerId,
                LocalDate.parse(transactionDate),
                new BigDecimal(amount),
                category,
                description));
    }

    private static Map<String, Customer> indexCustomers(List<Customer> loadedCustomers) {
        Map<String, Customer> indexedCustomers = new LinkedHashMap<String, Customer>();
        for (Customer customer : loadedCustomers) {
            if (indexedCustomers.put(customer.getCustomerId(), customer) != null) {
                throw new IllegalStateException("Duplicate customer id " + customer.getCustomerId() + ".");
            }
        }
        return Collections.unmodifiableMap(indexedCustomers);
    }

    private static List<Customer> immutableCustomers(List<Customer> loadedCustomers) {
        List<Customer> sortedCustomers = new ArrayList<Customer>(loadedCustomers);
        Collections.sort(sortedCustomers, Comparator.comparing(Customer::getCustomerId));
        return Collections.unmodifiableList(sortedCustomers);
    }

    private static Map<String, List<Purchase>> indexPurchases(List<Customer> loadedCustomers, List<Purchase> loadedPurchases) {
        Map<String, Customer> knownCustomers = new LinkedHashMap<String, Customer>();
        for (Customer customer : loadedCustomers) {
            knownCustomers.put(customer.getCustomerId(), customer);
        }

        Map<String, List<Purchase>> indexedPurchases = new LinkedHashMap<String, List<Purchase>>();
        for (Customer customer : loadedCustomers) {
            indexedPurchases.put(customer.getCustomerId(), new ArrayList<Purchase>());
        }

        Map<String, Purchase> purchasesById = new LinkedHashMap<String, Purchase>();
        for (Purchase purchase : loadedPurchases) {
            if (purchasesById.put(purchase.getTransactionId(), purchase) != null) {
                throw new IllegalStateException("Duplicate transaction id " + purchase.getTransactionId() + ".");
            }
            if (!knownCustomers.containsKey(purchase.getCustomerId())) {
                throw new IllegalStateException("Purchase " + purchase.getTransactionId()
                        + " refers to unknown customer " + purchase.getCustomerId() + ".");
            }
            indexedPurchases.get(purchase.getCustomerId()).add(purchase);
        }

        Map<String, List<Purchase>> frozenPurchases = new LinkedHashMap<String, List<Purchase>>();
        for (Map.Entry<String, List<Purchase>> entry : indexedPurchases.entrySet()) {
            frozenPurchases.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(frozenPurchases);
    }
}
