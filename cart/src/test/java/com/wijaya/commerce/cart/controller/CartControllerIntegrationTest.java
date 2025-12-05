package com.wijaya.commerce.cart.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wijaya.commerce.cart.BaseIntegrationTest;
import com.wijaya.commerce.cart.constant.CartApiPath;
import com.wijaya.commerce.cart.modelDb.CartModelDb;
import com.wijaya.commerce.cart.outbound.outboundModel.response.GetDetailProductOutboundResponse;
import com.wijaya.commerce.cart.outbound.outboundModel.response.GetDetailUserOutboundResponse;
import com.wijaya.commerce.cart.outbound.outboundModel.response.WebResponse;
import com.wijaya.commerce.cart.outbound.outboundService.ProductOutboundService;
import com.wijaya.commerce.cart.outbound.outboundService.UserOutboundService;
import com.wijaya.commerce.cart.restWebModel.request.AddToCartRequestWebModel;
import com.wijaya.commerce.cart.restWebModel.request.DeleteCartRequestWebModel;

class CartControllerIntegrationTest extends BaseIntegrationTest {

        @MockBean
        private ProductOutboundService productOutboundService;

        @MockBean
        private UserOutboundService userOutboundService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        // ==================== ADD TO CART TESTS ====================

        @Test
        void addToCart_Success_NewCart() throws Exception {
                // Mock user service response
                GetDetailUserOutboundResponse mockUser = GetDetailUserOutboundResponse.builder()
                                .id("user123")
                                .email("user123@example.com")
                                .name("Test User")
                                .status("ACTIVE")
                                .build();
                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse userWebResponse = WebResponse
                                .<GetDetailUserOutboundResponse>builder()
                                .success(true)
                                .data(mockUser)
                                .build();
                when(userOutboundService.getUserDetail(anyString())).thenReturn(userWebResponse);

                // Mock product service response
                GetDetailProductOutboundResponse mockProduct = GetDetailProductOutboundResponse.builder()
                                .sku("LAPTOP-001")
                                .name("Gaming Laptop")
                                .price(15000000L)
                                .comparePrice(18000000L)
                                .discountPercentage(17)
                                .active(true)
                                .build();

                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse productWebResponse = WebResponse
                                .<GetDetailProductOutboundResponse>builder()
                                .success(true)
                                .data(mockProduct)
                                .build();
                when(productOutboundService.getProductDetail(anyString())).thenReturn(productWebResponse);

                // Prepare request
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .productSku("LAPTOP-001")
                                .quantity(2)
                                .build();

                String userId = "user123";

                // Call API
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.cartId").exists())
                                .andExpect(jsonPath("$.data.items").isArray())
                                .andExpect(jsonPath("$.data.summary").exists())
                                .andExpect(jsonPath("$.data.items[0].productSku").value(mockProduct.getSku()))
                                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                                .andExpect(jsonPath("$.data.items[0].productName").value(mockProduct.getName()))
                                .andExpect(jsonPath("$.data.items[0].price").value(mockProduct.getPrice()))
                                .andExpect(jsonPath("$.data.items[0].comparePrice")
                                                .value(mockProduct.getComparePrice()))
                                .andExpect(jsonPath("$.data.items[0].discountPercentage")
                                                .value(mockProduct.getDiscountPercentage()))
                                .andExpect(jsonPath("$.data.items[0].subtotal").value(mockProduct.getPrice() * 2))
                                .andExpect(jsonPath("$.data.summary.totalItems").value(1))
                                .andExpect(jsonPath("$.data.summary.totalPrice").value(mockProduct.getPrice() * 2))
                                .andExpect(jsonPath("$.data.summary.totalDiscount").exists());

                // Verify database: cart should be created
                Optional<CartModelDb> cartInDb = cartRepository.findByUserId(userId);
                Assertions.assertTrue(cartInDb.isPresent(), "Cart should be created in database");
                Assertions.assertEquals(userId, cartInDb.get().getUserId());
                Assertions.assertNotNull(cartInDb.get().getItems());
                Assertions.assertEquals(mockProduct.getSku(), cartInDb.get().getItems().get(0).getProductSku());
                Assertions.assertEquals(mockProduct.getName(), cartInDb.get().getItems().get(0).getProductName());
                Assertions.assertEquals(mockProduct.getPrice(), cartInDb.get().getItems().get(0).getPrice());
                Assertions.assertEquals(mockProduct.getComparePrice(),
                                cartInDb.get().getItems().get(0).getComparePrice());
                Assertions.assertEquals(mockProduct.getDiscountPercentage(),
                                cartInDb.get().getItems().get(0).getDiscountPercentage());
                Assertions.assertEquals((int) (mockProduct.getPrice() * 2),
                                cartInDb.get().getItems().get(0).getSubTotal());
                Assertions.assertNotNull(cartInDb.get().getItems().get(0).getAddedAt());
        }

        @Test
        void addToCart_Success_ExistingCart() throws Exception {
                // Mock user service response
                GetDetailUserOutboundResponse mockUser = GetDetailUserOutboundResponse.builder()
                                .id("user456")
                                .email("user456@example.com")
                                .name("Test User 2")
                                .status("ACTIVE")
                                .build();
                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse userWebResponse = WebResponse
                                .<GetDetailUserOutboundResponse>builder()
                                .success(true)
                                .data(mockUser)
                                .build();
                when(userOutboundService.getUserDetail(anyString())).thenReturn(userWebResponse);

                // Mock product service response

                GetDetailProductOutboundResponse mockProduct = GetDetailProductOutboundResponse.builder()
                                .sku("LAPTOP-002")
                                .name("Business Laptop")
                                .price(12000000L)
                                .comparePrice(14000000L)
                                .discountPercentage(14)
                                .active(true)
                                .build();
                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse productWebResponse = WebResponse
                                .<GetDetailProductOutboundResponse>builder()
                                .success(true)
                                .data(mockProduct)
                                .build();
                when(productOutboundService.getProductDetail(anyString())).thenReturn(productWebResponse);

                // Pre-seed cart in database
                String userId = "user456";
                CartModelDb existingCart = CartModelDb.builder()
                                .id(new ObjectId())
                                .userId(userId)
                                .items(new ArrayList<>())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                // Add initial item
                CartModelDb.CartItem initialItem = CartModelDb.CartItem.builder()
                                .productSku("PHONE-001")
                                .productName("Smartphone")
                                .price(5000000L)
                                .quantity(1)
                                .subTotal(5000000)
                                .addedAt(LocalDateTime.now())
                                .build();
                existingCart.getItems().add(initialItem);

                CartModelDb savedCart = cartRepository.save(existingCart);
                String cartId = savedCart.getId().toString();

                // Prepare request to add new item
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .cartId(cartId)
                                .productSku("LAPTOP-002")
                                .quantity(2)
                                .build();

                // Call API
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.cartId").value(cartId))
                                .andExpect(jsonPath("$.data.items").isArray())
                                .andExpect(jsonPath("$.data.summary").exists())
                                .andExpect(jsonPath("$.data.items[1].productSku").value(mockProduct.getSku()))
                                .andExpect(jsonPath("$.data.items[1].quantity").value(2))
                                .andExpect(jsonPath("$.data.items[1].productName").value(mockProduct.getName()))
                                .andExpect(jsonPath("$.data.items[1].price").value(mockProduct.getPrice()))
                                .andExpect(jsonPath("$.data.items[1].comparePrice")
                                                .value(mockProduct.getComparePrice()))
                                .andExpect(jsonPath("$.data.items[1].discountPercentage")
                                                .value(mockProduct.getDiscountPercentage()))
                                .andExpect(jsonPath("$.data.items[1].subtotal").value(mockProduct.getPrice() * 2))
                                .andExpect(jsonPath("$.data.summary.totalItems").value(2))
                                .andExpect(jsonPath("$.data.summary.totalPrice").value((mockProduct.getPrice() * 2)
                                                + savedCart.getItems().get(0).getSubTotal()))
                                .andExpect(jsonPath("$.data.summary.totalDiscount").exists());
                ;

                // Verify database: cart should have 2 items
                Optional<CartModelDb> updatedCart = cartRepository.findById(cartId);
                Assertions.assertTrue(updatedCart.isPresent());
                Assertions.assertEquals(2, updatedCart.get().getItems().size());
        }

        @Test
        void addToCart_ValidationError_MissingProductSku() throws Exception {
                // Prepare request without productSku
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .quantity(1)
                                .build();

                String userId = "user789";

                // Call API
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data.productSku").value("Product SKU is required"));

                // Verify database: no cart should be created
                Optional<CartModelDb> cartInDb = cartRepository.findByUserId(userId);
                Assertions.assertFalse(cartInDb.isPresent(), "No cart should be created for invalid request");
        }

        @Test
        void addToCart_ValidationError_InvalidQuantity() throws Exception {
                // Prepare request with invalid quantity (0)
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .productSku("LAPTOP-003")
                                .quantity(0)
                                .build();

                String userId = "user000";

                // Call API
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data.quantity").value("Quantity must be at least 1"));

                // Verify database: no cart should be created
                Optional<CartModelDb> cartInDb = cartRepository.findByUserId(userId);
                Assertions.assertFalse(cartInDb.isPresent(), "No cart should be created for invalid quantity");
        }

        @Test
        void addToCart_ValidationError_NotSendQuantity() throws Exception {
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .productSku("LAPTOP-003")
                                .build();

                String userId = "user000";

                // Call API
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data.quantity").value("Quantity is required"));

                // Verify database: no cart should be created
                Optional<CartModelDb> cartInDb = cartRepository.findByUserId(userId);
                Assertions.assertFalse(cartInDb.isPresent(), "No cart should be created for invalid quantity");
        }

        @Test
        void addToCart_ValidationError_MissingUserId() throws Exception {
                // Prepare request
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .productSku("LAPTOP-004")
                                .quantity(1)
                                .build();

                // Call API without X-User-Id header
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data").value("Required request header 'X-User-Id' is missing"));
        }

        @Test
        void addToCart_Error_UserNotFound() throws Exception {
                // Mock user service error response - user not found
                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse userWebResponse = WebResponse
                                .<String>builder()
                                .success(false)
                                .data("User not found")
                                .build();
                when(userOutboundService.getUserDetail(anyString())).thenReturn(userWebResponse);

                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse productWebResponse = WebResponse
                                .<GetDetailProductOutboundResponse>builder()
                                .success(true)
                                .data(GetDetailProductOutboundResponse.builder()
                                                .sku("LAPTOP-001")
                                                .name("Gaming Laptop")
                                                .price(15000000L)
                                                .comparePrice(18000000L)
                                                .discountPercentage(17)
                                                .active(true)
                                                .build())
                                .build();
                when(productOutboundService.getProductDetail(anyString())).thenReturn(productWebResponse);

                // Prepare request
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .productSku("LAPTOP-001")
                                .quantity(2)
                                .build();

                String userId = "user123";

                // Call API - expect 418 I'm a Teapot status with error message
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isIAmATeapot())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data").value("User not found"));

                // Verify database: no cart should be created
                Optional<CartModelDb> cartInDb = cartRepository.findByUserId(userId);
                Assertions.assertFalse(cartInDb.isPresent(), "No cart should be created when user not found");
        }

        @Test
        void addToCart_Error_UserInactive() throws Exception {
                // Mock user service response
                GetDetailUserOutboundResponse mockUser = GetDetailUserOutboundResponse.builder()
                                .id("user123")
                                .email("user123@example.com")
                                .name("Test User")
                                .status("INACTIVE")
                                .build();
                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse userWebResponse = WebResponse
                                .<GetDetailUserOutboundResponse>builder()
                                .success(true)
                                .data(mockUser)
                                .build();
                when(userOutboundService.getUserDetail(anyString())).thenReturn(userWebResponse);

                // Mock product service response
                GetDetailProductOutboundResponse mockProduct = GetDetailProductOutboundResponse.builder()
                                .sku("LAPTOP-001")
                                .name("Gaming Laptop")
                                .price(15000000L)
                                .comparePrice(18000000L)
                                .discountPercentage(17)
                                .active(true)
                                .build();

                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse productWebResponse = WebResponse
                                .<GetDetailProductOutboundResponse>builder()
                                .success(true)
                                .data(mockProduct)
                                .build();
                when(productOutboundService.getProductDetail(anyString())).thenReturn(productWebResponse);

                // Prepare request
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .productSku("LAPTOP-001")
                                .quantity(2)
                                .build();

                String userId = "user123";

                // Call API
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isIAmATeapot())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data").value("User not active"));

                // Verify database: cart should be created
                Optional<CartModelDb> cartInDb = cartRepository.findByUserId(userId);
                Assertions.assertFalse(cartInDb.isPresent(), "Cart should be created in database");
        }

        @Test
        void addToCart_Error_ProductNotFound() throws Exception {
                // Mock user service response
                GetDetailUserOutboundResponse mockUser = GetDetailUserOutboundResponse.builder()
                                .id("user123")
                                .email("user123@example.com")
                                .name("Test User")
                                .status("ACTIVE")
                                .build();
                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse userWebResponse = WebResponse
                                .<GetDetailUserOutboundResponse>builder()
                                .success(true)
                                .data(mockUser)
                                .build();
                when(userOutboundService.getUserDetail(anyString())).thenReturn(userWebResponse);

                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse productWebResponse = WebResponse
                                .<String>builder()
                                .success(false)
                                .data("Product with sku LAPTOP-001 not found")
                                .build();
                when(productOutboundService.getProductDetail(anyString())).thenReturn(productWebResponse);

                // Prepare request
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .productSku("LAPTOP-001")
                                .quantity(2)
                                .build();

                String userId = "user123";

                // Call API
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isIAmATeapot())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data").value("Product not found"));

                // Verify database: cart should be created
                Optional<CartModelDb> cartInDb = cartRepository.findByUserId(userId);
                Assertions.assertFalse(cartInDb.isPresent(), "Cart should not be created in database");
        }

        @Test
        void addToCart_Error_ProductDisabled() throws Exception {
                // Mock user service response
                GetDetailUserOutboundResponse mockUser = GetDetailUserOutboundResponse.builder()
                                .id("user123")
                                .email("user123@example.com")
                                .name("Test User")
                                .status("ACTIVE")
                                .build();
                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse userWebResponse = WebResponse
                                .<GetDetailUserOutboundResponse>builder()
                                .success(true)
                                .data(mockUser)
                                .build();
                when(userOutboundService.getUserDetail(anyString())).thenReturn(userWebResponse);

                // Mock product service response
                GetDetailProductOutboundResponse mockProduct = GetDetailProductOutboundResponse.builder()
                                .sku("LAPTOP-001")
                                .name("Gaming Laptop")
                                .price(15000000L)
                                .comparePrice(18000000L)
                                .discountPercentage(17)
                                .active(false)
                                .build();

                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse productWebResponse = WebResponse
                                .<GetDetailProductOutboundResponse>builder()
                                .success(true)
                                .data(mockProduct)
                                .build();
                when(productOutboundService.getProductDetail(anyString())).thenReturn(productWebResponse);

                // Prepare request
                AddToCartRequestWebModel request = AddToCartRequestWebModel.builder()
                                .productSku("LAPTOP-001")
                                .quantity(2)
                                .build();

                String userId = "user123";

                // Call API
                mockMvc.perform(post(CartApiPath.ADD_TO_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isIAmATeapot())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data").value("Product not active"));

                // Verify database: cart should be created
                Optional<CartModelDb> cartInDb = cartRepository.findByUserId(userId);
                Assertions.assertFalse(cartInDb.isPresent(), "Cart should be created in database");
        }

        // ==================== CLEAR ALL CART TESTS ====================

        @Test
        void clearAllCart_Success() throws Exception {
                // Mock user service response (for validation if needed)
                GetDetailUserOutboundResponse mockUser = GetDetailUserOutboundResponse.builder()
                                .id("user999")
                                .email("user999@example.com")
                                .name("Test User 999")
                                .status("ACTIVE")
                                .build();
                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse userWebResponse = WebResponse
                                .<GetDetailUserOutboundResponse>builder()
                                .success(true)
                                .data(mockUser)
                                .build();
                when(userOutboundService.getUserDetail(anyString())).thenReturn(userWebResponse);

                // Pre-seed cart with multiple items
                String userId = "user999";
                CartModelDb cart = CartModelDb.builder()
                                .id(new ObjectId())
                                .userId(userId)
                                .items(new ArrayList<>())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                // Add multiple items
                cart.getItems().add(CartModelDb.CartItem.builder()
                                .productSku("ITEM-001")
                                .productName("Item 1")
                                .price(100000L)
                                .quantity(2)
                                .subTotal(200000)
                                .addedAt(LocalDateTime.now())
                                .build());

                cart.getItems().add(CartModelDb.CartItem.builder()
                                .productSku("ITEM-002")
                                .productName("Item 2")
                                .price(200000L)
                                .quantity(1)
                                .subTotal(200000)
                                .addedAt(LocalDateTime.now())
                                .build());

                CartModelDb savedCart = cartRepository.save(cart);
                String cartId = savedCart.getId().toString();

                // Prepare request
                DeleteCartRequestWebModel request = DeleteCartRequestWebModel.builder()
                                .cartId(cartId)
                                .build();

                // Call API
                mockMvc.perform(post(CartApiPath.CLEAR_ALL_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.cartId").value(cartId))
                                .andExpect(jsonPath("$.data.message").value("Cart deleted"));

                // Verify database: cart should be deleted
                Optional<CartModelDb> deletedCart = cartRepository.findById(cartId);
                Assertions.assertFalse(deletedCart.isPresent(), "Cart should be deleted from database");
        }

        @Test
        void clearAllCart_NotFound_InvalidCartId() throws Exception {
                // Prepare request with non-existent cart ID
                String nonExistentCartId = new ObjectId().toString();
                DeleteCartRequestWebModel request = DeleteCartRequestWebModel.builder()
                                .cartId(nonExistentCartId)
                                .build();

                String userId = "user888";

                // Call API - expect 418 I_AM_A_TEAPOT with "Cart not found" message
                mockMvc.perform(post(CartApiPath.CLEAR_ALL_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().is(418))
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data").value("Cart not found"));

                // Verify database: no cart with this ID exists
                Optional<CartModelDb> cart = cartRepository.findById(nonExistentCartId);
                Assertions.assertFalse(cart.isPresent());
        }

        @Test
        void clearAllCart_ValidationError_MissingCartId() throws Exception {
                // Prepare request without cartId
                DeleteCartRequestWebModel request = DeleteCartRequestWebModel.builder()
                                .build();

                String userId = "user777";

                // Call API
                mockMvc.perform(post(CartApiPath.CLEAR_ALL_CART)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data.cartId").value("Cart ID is required"));
        }

        @Test
        void clearAllCart_Forbidden_WrongUser() throws Exception {
                // Mock user service response
                GetDetailUserOutboundResponse mockUser = GetDetailUserOutboundResponse.builder()
                                .id("user222")
                                .email("user222@example.com")
                                .name("Test User 222")
                                .status("ACTIVE")
                                .build();
                @SuppressWarnings({ "rawtypes", "unchecked" })
                WebResponse userWebResponse = WebResponse
                                .<GetDetailUserOutboundResponse>builder()
                                .success(true)
                                .data(mockUser)
                                .build();
                when(userOutboundService.getUserDetail(anyString())).thenReturn(userWebResponse);

                // Pre-seed cart for user1
                String user1 = "user111";
                CartModelDb cart = CartModelDb.builder()
                                .id(new ObjectId())
                                .userId(user1)
                                .items(new ArrayList<>())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                CartModelDb savedCart = cartRepository.save(cart);
                String cartId = savedCart.getId().toString();

                // Prepare request
                DeleteCartRequestWebModel request = DeleteCartRequestWebModel.builder()
                                .cartId(cartId)
                                .build();

                // Try to delete with different user
                String user2 = "user222";

                // Call API - expect 418 I_AM_A_TEAPOT with "Cart not found" message (since
                // findByIdAndUserId will fail)
                mockMvc.perform(post(CartApiPath.CLEAR_ALL_CART)
                                .header("X-User-Id", user2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isIAmATeapot())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.data").value("Cart not found"));

                // Verify database: cart should still exist (not deleted)
                Optional<CartModelDb> stillExistingCart = cartRepository.findById(cartId);
                Assertions.assertTrue(stillExistingCart.isPresent(),
                                "Cart should still exist when deletion attempted by wrong user");
                Assertions.assertEquals(user1, stillExistingCart.get().getUserId());
        }
}
