import React, { createContext, useState, useContext, ReactNode } from 'react';

interface CartItem {
  itemId: number;
  quantity: number;
}

interface CartContextType {
  cartItems: CartItem[];
  addToCart: (item: CartItem) => void;
  removeFromCart: (itemId: number) => void;
  updateQuantity: (itemId: number, quantity: number) => void;
  clearCart: () => void;
  getTotalItems: () => number;
}

export const CartContext = createContext<CartContextType | undefined>(undefined);

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within CartProvider');
  }
  return context;
};

interface CartProviderProps {
  children: ReactNode;
}

export const CartProvider = ({ children }: CartProviderProps) => {
  const [cartItems, setCartItems] = useState<CartItem[]>(() => {
    const savedCart = localStorage.getItem('cart');
    return savedCart ? JSON.parse(savedCart) : [];
  });

  const saveCartToStorage = (items: CartItem[]) => {
    localStorage.setItem('cart', JSON.stringify(items));
  };

  const addToCart = (item: CartItem) => {
    setCartItems(prevItems => {
      const existingItemIndex = prevItems.findIndex(i => i.itemId === item.itemId);
      let newItems;

      if (existingItemIndex !== -1) {
        newItems = [...prevItems];
        newItems[existingItemIndex].quantity += item.quantity;
      } else {
        newItems = [...prevItems, item];
      }

      saveCartToStorage(newItems);
      return newItems;
    });
  };

  const removeFromCart = (itemId: number) => {
    setCartItems(prevItems => {
      const newItems = prevItems.filter(item => item.itemId !== itemId);
      saveCartToStorage(newItems);
      return newItems;
    });
  };

  const updateQuantity = (itemId: number, quantity: number) => {
    if (quantity < 1) {
      removeFromCart(itemId);
      return;
    }

    setCartItems(prevItems => {
      const newItems = prevItems.map(item =>
        item.itemId === itemId ? { ...item, quantity } : item
      );
      saveCartToStorage(newItems);
      return newItems;
    });
  };

  const clearCart = () => {
    setCartItems([]);
    localStorage.removeItem('cart');
  };

  const getTotalItems = () => {
    return cartItems.reduce((total, item) => total + item.quantity, 0);
  };

  return (
    <CartContext.Provider value={{
      cartItems,
      addToCart,
      removeFromCart,
      updateQuantity,
      clearCart,
      getTotalItems
    }}>
      {children}
    </CartContext.Provider>
  );
};