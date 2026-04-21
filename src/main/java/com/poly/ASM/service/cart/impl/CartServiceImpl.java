package com.poly.ASM.service.cart.impl;

import com.poly.ASM.entity.product.Product;
import com.poly.ASM.entity.product.ProductSize;
import com.poly.ASM.entity.product.Size;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.repository.cart.CartItemRepository;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.service.cart.CartItem;
import com.poly.ASM.service.cart.CartService;
import com.poly.ASM.service.product.ProductService;
import com.poly.ASM.service.product.ProductSizeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    /**
     * Anonymous cart key in session (new design).
     * Legacy key exists in older builds: CART_ITEMS.
     */
    private static final String SESSION_CART_KEY = "SESSION_CART";
    private static final String LEGACY_CART_SESSION_KEY = "CART_ITEMS";

    private final HttpSession session;
    private final ProductService productService;
    private final ProductSizeService productSizeService;
    private final AuthService authService;
    private final CartItemRepository cartItemRepository;

    @Override
    public boolean addToCart(Integer productId, Integer sizeId, Integer quantity) {
        if (isAuthenticated()) {
            return addToUserCart(authService.getUser().getUsername(), productId, sizeId, quantity);
        }
        return addToSessionCart(productId, sizeId, quantity);
    }

    @Override
    public List<CartItem> getCartItems() {
        if (isAuthenticated()) {
            return getUserCartItems(authService.getUser().getUsername());
        }
        return getSessionCartItems(false);
    }

    @Override
    public void mergeSessionCartToUserCart(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        List<CartItem> sessionItems = getSessionCartItems(false);
        if (sessionItems.isEmpty()) {
            return;
        }
        for (CartItem item : sessionItems) {
            if (item == null || item.getProductId() == null || item.getSizeId() == null || item.getQuantity() == null) {
                continue;
            }
            int qty = item.getQuantity();
            if (qty <= 0) {
                continue;
            }
            mergeIntoUserCart(username, item.getProductId(), item.getSizeId(), qty);
        }
        clearSessionCart();
    }

    @Override
    public void clearSessionCart() {
        session.removeAttribute(SESSION_CART_KEY);
        session.removeAttribute(LEGACY_CART_SESSION_KEY);
    }

    @Override
    @Transactional
    public void clearCart() {
        if (isAuthenticated()) {
            cartItemRepository.deleteByAccountUsername(authService.getUser().getUsername());
            return;
        }
        clearSessionCart();
    }

    @Override
    public long getDistinctProductCount() {
        if (isAuthenticated()) {
            return cartItemRepository.countDistinctProductsByUsername(authService.getUser().getUsername());
        }
        return getSessionCartItems(false)
                .stream()
                .map(CartItem::getProductId)
                .distinct()
                .count();
    }

    @Override
    public Set<Integer> getProductIdsInCart() {
        if (isAuthenticated()) {
            return new HashSet<>(cartItemRepository.findDistinctProductIdsByUsername(authService.getUser().getUsername()));
        }
        return getSessionCartItems(false)
                .stream()
                .map(CartItem::getProductId)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public boolean add(Integer productId, Integer sizeId) {
        return addToCart(productId, sizeId, 1);
    }

    @Override
    public boolean add(Integer productId, Integer sizeId, Integer quantity) {
        return addToCart(productId, sizeId, quantity);
    }

    @Override
    public void remove(Integer productId, Integer sizeId) {
        if (isAuthenticated()) {
            cartItemRepository.findByAccountUsernameAndProductIdAndSizeId(authService.getUser().getUsername(), productId, sizeId)
                    .ifPresent(cartItemRepository::delete);
            return;
        }
        List<CartItem> items = getSessionCartItems(true);
        items.removeIf(item -> item.getProductId().equals(productId) && item.getSizeId().equals(sizeId));
        saveSessionItems(items);
    }

    @Override
    public boolean update(Integer productId, Integer sizeId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            remove(productId, sizeId);
            return true;
        }

        Optional<ProductSize> productSize = productSizeService.findByProductIdAndSizeId(productId, sizeId);
        if (productSize.isEmpty() || productSize.get().getQuantity() == null) {
            return false;
        }
        if (quantity > productSize.get().getQuantity()) {
            return false;
        }

        if (isAuthenticated()) {
            var opt = cartItemRepository.findByAccountUsernameAndProductIdAndSizeId(authService.getUser().getUsername(), productId, sizeId);
            if (opt.isEmpty()) {
                return false;
            }
            var entity = opt.get();
            entity.setQuantity(quantity);
            cartItemRepository.save(entity);
            return true;
        }

        List<CartItem> items = getSessionCartItems(true);
        for (CartItem item : items) {
            if (item.getProductId().equals(productId) && item.getSizeId().equals(sizeId)) {
                item.setQuantity(quantity);
                break;
            }
        }
        saveSessionItems(items);
        return true;
    }

    @Override
    public void clear() {
        clearCart();
    }

    @Override
    public List<CartItem> getItems() {
        return getCartItems();
    }

    @Override
    public BigDecimal getTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : getCartItems()) {
            if (item.getPrice() != null && item.getQuantity() != null) {
                BigDecimal unitPrice = item.getPrice();
                BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                BigDecimal discountPercent = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;
                
                BigDecimal lineTotal = unitPrice.multiply(qty);
                if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal discountAmount = lineTotal.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    lineTotal = lineTotal.subtract(discountAmount);
                }
                
                total = total.add(lineTotal);
            }
        }
        return total;
    }

    private boolean isAuthenticated() {
        return authService != null && authService.isAuthenticated() && authService.getUser() != null;
    }

    private List<CartItem> getSessionCartItems(boolean createIfMissing) {
        Object value = session.getAttribute(SESSION_CART_KEY);
        if (!(value instanceof List<?>)) {
            // Backward compatibility: old session key from previous builds
            value = session.getAttribute(LEGACY_CART_SESSION_KEY);
            if (value instanceof List<?>) {
                session.setAttribute(SESSION_CART_KEY, value);
                session.removeAttribute(LEGACY_CART_SESSION_KEY);
            }
        }
        if (value instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<CartItem> items = (List<CartItem>) value;
            return items;
        }
        List<CartItem> items = new ArrayList<>();
        if (createIfMissing) {
            saveSessionItems(items);
        }
        return items;
    }

    private void saveSessionItems(List<CartItem> items) {
        session.setAttribute(SESSION_CART_KEY, items);
    }

    private boolean addToSessionCart(Integer productId, Integer sizeId, Integer quantity) {
        List<CartItem> items = getSessionCartItems(true);
        if (sizeId == null || quantity == null || quantity <= 0) {
            return false;
        }
        Optional<ProductSize> productSize = productSizeService.findByProductIdAndSizeId(productId, sizeId);
        if (productSize.isEmpty() || productSize.get().getQuantity() == null || productSize.get().getQuantity() <= 0) {
            return false;
        }
        String sizeName = productSize.get().getSize() != null ? productSize.get().getSize().getName() : null;
        for (CartItem item : items) {
            if (item.getProductId().equals(productId) && item.getSizeId().equals(sizeId)) {
                int nextQuantity = item.getQuantity() + quantity;
                if (nextQuantity <= productSize.get().getQuantity()) {
                    item.setQuantity(nextQuantity);
                    saveSessionItems(items);
                    return true;
                }
                return false;
            }
        }

        Optional<Product> productOpt = productService.findById(productId);
        if (productOpt.isEmpty()) {
            return false;
        }
        Product product = productOpt.get();
        if (quantity > productSize.get().getQuantity()) {
            return false;
        }
        CartItem newItem = new CartItem(
                product.getId(),
                product.getName(),
                sizeId,
                sizeName,
                product.getPrice(),
                product.getDiscount(),
                quantity,
                product.getImage(),
                productSize.get().getQuantity()
        );
        items.add(newItem);
        saveSessionItems(items);
        return true;
    }

    private List<CartItem> getUserCartItems(String username) {
        var entities = cartItemRepository.findByUsernameWithRefs(username);
        LinkedHashMap<String, CartItem> merged = new LinkedHashMap<>();
        for (var ci : entities) {
            var p = ci.getProduct();
            var s = ci.getSize();
            Integer stock = null;
            if (p != null && s != null) {
                stock = productSizeService.findByProductIdAndSizeId(p.getId(), s.getId())
                        .map(ProductSize::getQuantity)
                        .orElse(null);
            }
            Integer pid = p != null ? p.getId() : null;
            Integer sid = s != null ? s.getId() : null;
            if (pid == null || sid == null) {
                continue;
            }
            String key = pid + "::" + sid;
            CartItem existing = merged.get(key);
            if (existing == null) {
                merged.put(key, new CartItem(
                        pid,
                        p.getName(),
                        sid,
                        s.getName(),
                        p.getPrice(),
                        p.getDiscount(),
                        ci.getQuantity(),
                        p.getImage(),
                        stock
                ));
            } else {
                int nextQty = (existing.getQuantity() == null ? 0 : existing.getQuantity()) + (ci.getQuantity() == null ? 0 : ci.getQuantity());
                existing.setQuantity(nextQty);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private boolean addToUserCart(String username, Integer productId, Integer sizeId, Integer quantity) {
        if (username == null || username.isBlank()) {
            return false;
        }
        if (sizeId == null || quantity == null || quantity <= 0) {
            return false;
        }
        Optional<ProductSize> productSize = productSizeService.findByProductIdAndSizeId(productId, sizeId);
        if (productSize.isEmpty() || productSize.get().getQuantity() == null || productSize.get().getQuantity() <= 0) {
            return false;
        }
        int stock = productSize.get().getQuantity();

        var existingItems = cartItemRepository.findAllByAccountUsernameAndProductIdAndSizeIdOrderByIdAsc(username, productId, sizeId);
        if (!existingItems.isEmpty()) {
            var existing = existingItems.get(0);
            int current = existingItems.stream().mapToInt(x -> x.getQuantity() == null ? 0 : x.getQuantity()).sum();
            int next = current + quantity;
            if (next > stock) {
                return false;
            }
            existing.setQuantity(next);
            cartItemRepository.save(existing);
            if (existingItems.size() > 1) {
                cartItemRepository.deleteAll(existingItems.subList(1, existingItems.size()));
            }
            return true;
        }

        if (quantity > stock) {
            return false;
        }

        Account acc = new Account();
        acc.setUsername(username);
        Product p = new Product();
        p.setId(productId);
        Size s = new Size();
        s.setId(sizeId);

        var entity = com.poly.ASM.entity.cart.CartItemEntity.builder()
                .account(acc)
                .product(p)
                .size(s)
                .quantity(quantity)
                .build();
        cartItemRepository.save(entity);
        return true;
    }

    private void mergeIntoUserCart(String username, Integer productId, Integer sizeId, Integer quantity) {
        if (username == null || username.isBlank() || productId == null || sizeId == null || quantity == null || quantity <= 0) {
            return;
        }
        Optional<ProductSize> productSize = productSizeService.findByProductIdAndSizeId(productId, sizeId);
        if (productSize.isEmpty() || productSize.get().getQuantity() == null || productSize.get().getQuantity() <= 0) {
            return;
        }
        int stock = productSize.get().getQuantity();

        var existingItems = cartItemRepository.findAllByAccountUsernameAndProductIdAndSizeIdOrderByIdAsc(username, productId, sizeId);
        if (!existingItems.isEmpty()) {
            var existing = existingItems.get(0);
            int current = existingItems.stream().mapToInt(x -> x.getQuantity() == null ? 0 : x.getQuantity()).sum();
            int merged = current + quantity;
            existing.setQuantity(Math.min(merged, stock));
            cartItemRepository.save(existing);
            if (existingItems.size() > 1) {
                cartItemRepository.deleteAll(existingItems.subList(1, existingItems.size()));
            }
            return;
        }

        Account acc = new Account();
        acc.setUsername(username);
        Product p = new Product();
        p.setId(productId);
        Size s = new Size();
        s.setId(sizeId);
        var entity = com.poly.ASM.entity.cart.CartItemEntity.builder()
                .account(acc)
                .product(p)
                .size(s)
                .quantity(Math.min(quantity, stock))
                .build();
        cartItemRepository.save(entity);
    }
}
