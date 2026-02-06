import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { OrderDTO, orderApi } from '../api/client';
import { useAuth } from '../contexts/AuthContext';

const OrdersPage: React.FC = () => {
  const { user } = useAuth();
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [debugInfo, setDebugInfo] = useState<string>('');

  const fetchUserOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      setDebugInfo('Starting to load orders...');

      const userId = localStorage.getItem('uuid');
      const token = localStorage.getItem('token');

      if (!userId) {
        const errorMsg = 'User ID not found. Please log in again.';
        setError(errorMsg);
        setDebugInfo(prev => prev + errorMsg);
        setLoading(false);
        return;
      }

      if (!token) {
        const errorMsg = 'Token not found. Please log in again.';
        setError(errorMsg);
        setDebugInfo(prev => prev + errorMsg);
        setLoading(false);
        return;
      }

      console.log('🔍 [OrdersPage] Loading orders for user ID:', userId);
      console.log('🔍 [OrdersPage] Token:', token.substring(0, 30) + '...');

      const endpoint = `/orders/users/internal/${userId}`;
      setDebugInfo(prev => prev + `\n🔍 Endpoint: ${endpoint}`);
      setDebugInfo(prev => prev + `\n🔍 UserId: ${userId}`);

      const response = await orderApi.getOrdersByUserId(userId);

      setDebugInfo(prev => prev + `Response status: ${response.status}`);

      if (response.status === 400) {
        setDebugInfo(prev => prev + 'Server returned 400 Bad Request');

        if (response.data) {
          console.log('[OrdersPage] Data at 400:', response.data);
          setDebugInfo(prev => prev + 'Data at 400: ' + JSON.stringify(response.data));

          if (Array.isArray(response.data)) {
            setOrders(response.data);
            localStorage.setItem('userOrders', JSON.stringify(response.data));
            setDebugInfo(prev => prev + 'Using data from response 400');
          } else {
            setOrders([]);
            setError('The server returned an error, but no orders were found');
          }
        } else {
          setOrders([]);
          setError('No orders found or the server returned an error.');
          setDebugInfo(prev => prev + 'The server response is empty at 400');
        }
      }
      else if (response.status === 200) {
        if (response.data && Array.isArray(response.data) && response.data.length > 0) {
          setOrders(response.data);
          localStorage.setItem('userOrders', JSON.stringify(response.data));
          setDebugInfo(prev => prev + `Orders received: ${response.data.length}`);
        } else {
          setOrders([]);
          setError('No orders found');
          setDebugInfo(prev => prev + 'No orders found');
        }
      }

    } catch (err: any) {
      console.error('[OrdersPage] Error loading orders:', err);

      let errorMessage = 'Failed to load orders';

      if (err.response) {
        console.error('[OrdersPage] Error data:', {
          status: err.response.status,
          statusText: err.response.statusText,
          data: err.response.data,
          headers: err.response.headers
        });

        setDebugInfo(prev => prev + `Error data: ${JSON.stringify(err.response.data)}`);

        if (err.response.status === 401) {
          errorMessage = 'Authorization required. Please log in again..';
        } else if (err.response.status === 403) {
          errorMessage = 'No access to orders';
        } else if (err.response.status === 404) {
          errorMessage = 'No orders found';
        } else if (err.response.status === 500) {
          errorMessage = 'Server error. Try again later';
        }
      } else if (err.request) {
        setDebugInfo(prev => prev + 'Request error: No response from server');
        errorMessage = 'No response from the server. Check your connection';
      } else {
        setDebugInfo(prev => prev + `Configuration error: ${err.message}`);
        errorMessage = `Error: ${err.message}`;
      }

      setError(errorMessage);

      const savedOrders = localStorage.getItem('userOrders');
      if (savedOrders) {
        try {
          const parsedOrders = JSON.parse(savedOrders);
          if (Array.isArray(parsedOrders) && parsedOrders.length > 0) {
            setOrders(parsedOrders);
            setDebugInfo(prev => prev + ` Loading from cash: ${parsedOrders.length} orders`);
          }
        } catch (parseError) {
          console.error('[OrdersPage] Error parsing saved orders:', parseError);
        }
      }
    } finally {
      setLoading(false);
      setDebugInfo(prev => prev + `\n⏱️  Finished at: ${new Date().toLocaleTimeString()}`);
    }
  };

  useEffect(() => {
    if (user) {
      fetchUserOrders();
    } else {
      setLoading(false);
      setError('Authorization required');
    }
  }, [user]);

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateString;
    }
  };

  const getStatusBadge = (status: OrderDTO['status']) => {
    const statusMap: Record<string, { className: string; label: string }> = {
      PENDING: { className: 'badge bg-warning text-dark', label: 'PENDING' },
      PROCESSING: { className: 'badge bg-info', label: 'PROCESSING' },
      COMPLETED: { className: 'badge bg-success', label: 'COMPLETED' },
      PAYMENT_FAILED: { className: 'badge bg-danger', label: 'PAYMENT FAILED' },
      CANCELLED: { className: 'badge bg-secondary', label: 'CANCELLED' },
    };

    return statusMap[status] || { className: 'badge bg-light text-dark', label: status };
  };

  const calculateTotal = (order: OrderDTO) => {
    return order.orderItems.reduce((total, item) => {
      return total + (item.itemPrice * item.quantity);
    }, 0);
  };

  const testDirectApiCall = async () => {
    const userId = localStorage.getItem('uuid');
    const token = localStorage.getItem('token');

    if (!userId || !token) {
      alert('No user ID or token');
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8077/api/v1/orders/users/internal/${userId}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          }
        }
      );
      const data = await response.json();
      console.log('Direct API test response:', data);
      alert(`Direct API test:\nStatus: ${response.status}\nData: ${JSON.stringify(data)}`);
    } catch (error) {
      console.error('Direct API test error:', error);
      alert('Direct API test error: ' + error);
    }
  };

  const clearCache = () => {
    localStorage.removeItem('userOrders');
    setOrders([]);
    setDebugInfo(prev => prev + '\n🧹 Cache cleared');
    alert('Order cache cleared');
  };

  const closeError = () => {
    setError(null);
  };

  if (loading) {
    return (
      <div className="container mt-4">
        <div className="d-flex justify-content-center align-items-center flex-column" style={{ minHeight: '50vh' }}>
          <div className="spinner-border text-primary" role="status" style={{ width: '3rem', height: '3rem' }}>
            <span className="visually-hidden">Loading...</span>
          </div>
          <h4 className="mt-3">Loading orders...</h4>
          <p className="text-muted">Please, wait</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="h3 mb-0">
          📦 MY ORDERS
        </h1>
        <div className="d-flex gap-2">
          <button
            onClick={fetchUserOrders}
            className="btn btn-outline-primary btn-sm"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                Refreshing...
              </>
            ) : (
              <>
                🔄 Refresh
              </>
            )}
          </button>
          <Link to="/orders/new" className="btn btn-primary btn-sm">
            ➕ New order
          </Link>
        </div>
      </div>

      <div className="card mb-4 border-primary">
        <div className="card-header bg-primary text-white">
          <h5 className="mb-0">👤 User information</h5>
        </div>
        <div className="card-body">
          <div className="row">
            <div className="col-md-6">
              <p><strong>User name:</strong> {user?.username || 'Unknown'}</p>
              <p><strong>Email:</strong> {user?.email || 'Not specified'}</p>
              <p><strong>Roles:</strong> {user?.roles?.join(', ') || 'USER'}</p>
            </div>
            <div className="col-md-6">
              <p>
                <strong>User:</strong><br />
                <code className="bg-light p-2 rounded d-block mt-1 small">
                  {localStorage.getItem('uuid') || 'Not found'}
                </code>
              </p>
              <p>
                <strong>Token:</strong><br />
                <span className={`badge ${localStorage.getItem('token') ? 'bg-success' : 'bg-danger'}`}>
                  {localStorage.getItem('token') ? 'Present' : 'Absent'}
                </span>
              </p>
            </div>
          </div>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger alert-dismissible fade show">
          <p>{error}</p>
          <button
            type="button"
            className="btn-close"
            onClick={closeError}
            aria-label="Close"
          ></button>
        </div>
      )}

      {orders.length === 0 && !error ? (
        <div className="alert alert-info">
          <h5 className="alert-heading">📭 There are no orders yet</h5>
          <p className="mb-3">You don't have any orders yet. Create your first order now!</p>
          <Link to="/orders/new" className="btn btn-primary">
            Create your first order
          </Link>
        </div>
      ) : (
        orders.length > 0 && (
          <>
            <div className="card mb-4">
              <div className="card-header bg-light">
                <div className="d-flex justify-content-between align-items-center">
                  <h5 className="mb-0">📋 Orders list</h5>
                  <span className="badge bg-primary rounded-pill">
                    Total: {orders.length}
                  </span>
                </div>
              </div>
              <div className="card-body p-0">
                <div className="table-responsive">
                  <table className="table table-hover mb-0">
                    <thead className="table-light">
                      <tr>
                        <th>📅 Creation date</th>
                        <th>🏷️ Status</th>
                        <th>📦 Items</th>
                        <th>💰 Sum</th>
                        <th>🆔 User ID</th>
                      </tr>
                    </thead>
                    <tbody>
                      {orders.map((order, index) => {
                        const statusInfo = getStatusBadge(order.status);
                        const total = calculateTotal(order);

                        return (
                          <tr key={index} className={index % 2 === 0 ? 'table-light' : ''}>
                            <td>
                              <div className="small text-muted">
                                {formatDate(order.creationDate)}
                              </div>
                            </td>
                            <td>
                              <span className={`badge ${statusInfo.className}`}>
                                {statusInfo.label}
                              </span>
                            </td>
                            <td>
                              <span className="badge bg-secondary">
                                {order.orderItems.length}
                              </span>
                            </td>
                            <td>
                              <strong className="text-success">{total.toFixed(2)} ₽</strong>
                            </td>
                            <td>
                              <code className="small" title={order.userId}>
                                {order.userId.substring(0, 8)}...
                              </code>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
              <div className="card-footer bg-light">
                <div className="d-flex justify-content-between align-items-center">
                  <small className="text-muted">
                    Update: {new Date().toLocaleTimeString('ru-RU')}
                  </small>
                </div>
              </div>
            </div>
          </>
        )
      )}
    </div>
  );
};

export default OrdersPage;