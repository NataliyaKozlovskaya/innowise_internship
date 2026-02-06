import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { PaymentDTO, PaymentStatus, paymentApi } from '../api/client';
import { useAuth } from '../contexts/AuthContext';

const PaymentsPage: React.FC = () => {
  const { user } = useAuth();
  const [payments, setPayments] = useState<PaymentDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterStatus, setFilterStatus] = useState<PaymentStatus | 'ALL'>('ALL');

  const fetchUserPayments = async () => {
    try {
      setLoading(true);
      setError(null);

      const userId = localStorage.getItem('uuid');

      if (!userId) {
        setError('User ID not found. Please log in again');
        setLoading(false);
        return;
      }

      const response = await paymentApi.getPaymentsByUserId(userId);

      console.log('Payments received:', response.data);

      if (response.data && response.data.length > 0) {
        setPayments(response.data);
        localStorage.setItem('userPayments', JSON.stringify(response.data));
      } else {
        setPayments([]);
        setError('Payments not found');
      }

    } catch (err: any) {
      console.error('Error loading payments:', err);

      if (err.response?.status === 404) {
        setError('Payments not found for the current user');
      } else if (err.response?.status === 401) {
        setError('Authorization required');
      } else if (err.response?.status === 403) {
        setError('No access to payments');
      } else if (err.response?.status === 500) {
        setError('Server error');
      } else {
        setError(err.response?.data?.message || 'Failed to load payments');
      }

      const savedPayments = localStorage.getItem('userPayments');
      if (savedPayments) {
        try {
          const parsedPayments = JSON.parse(savedPayments);
          setPayments(parsedPayments);
          setError(null);
        } catch (parseError) {
          console.error('Error parsing saved payments:', parseError);
        }
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      fetchUserPayments();
    } else {
      setLoading(false);
      setError('Authorization required');
    }
  }, [user]);

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('ru-RU', {
      style: 'currency',
      currency: 'RUB',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  const getStatusBadge = (status: PaymentStatus) => {
    const statusMap: Record<PaymentStatus, { className: string; label: string }> = {
      [PaymentStatus.PENDING]: { className: 'badge bg-warning', label: 'PENDING' },
      [PaymentStatus.COMPLETED]: { className: 'badge bg-success', label: 'COMPLETED' },
      [PaymentStatus.FAILED]: { className: 'badge bg-danger', label: 'FAILED' },
      [PaymentStatus.REFUNDED]: { className: 'badge bg-info', label: 'REFUNDED' },
      [PaymentStatus.CANCELLED]: { className: 'badge bg-secondary', label: 'CANCELLED' },
    };

    return statusMap[status] || { className: 'badge bg-light', label: status };
  };

  const filteredPayments = filterStatus === 'ALL'
    ? payments
    : payments.filter(payment => payment.status === filterStatus);

  if (loading) {
    return (
      <div className="container mt-4">
        <div className="d-flex justify-content-center">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
        <p className="text-center mt-2">Loading payments...</p>
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>MY PAYMENTS</h2>
        <div className="d-flex gap-2">
          <button
            onClick={fetchUserPayments}
            className="btn btn-outline-primary"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                Refreshing...
              </>
            ) : (
              <>
                <i className="bi bi-arrow-clockwise me-2"></i>
                Refresh
              </>
            )}
          </button>
        </div>
      </div>

      {payments.length === 0 && !error ? (
        <div className="alert alert-info">
          <h5>No payments yet</h5>
          <p>
            You don't have any payments yet.<br />
            After you place your order, your payments will appear here
          </p>
          <Link to="/orders/new" className="btn btn-primary mt-2">
            Create order
          </Link>
        </div>
      ) : (
        <>
          {payments.length > 0 && (
            <>
              <div className="alert alert-light mb-3">
                <h5 className="mb-0">Payments found: {payments.length}</h5>
              </div>

              <div className="table-responsive">
                <table className="table table-hover">
                  <thead className="table-light">
                    <tr>
                      <th>Order ID</th>
                      <th>User</th>
                      <th>Sum</th>
                      <th>Status</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {payments.map((payment, index) => {
                      const statusInfo = getStatusBadge(payment.status);
                      return (
                        <tr key={index}>
                          <td>
                            <Link to={`/orders/${payment.orderId}`} className="text-decoration-none">
                              <strong>#{payment.orderId}</strong>
                            </Link>
                          </td>
                          <td>
                            <small><code>{payment.userId}</code></small>
                          </td>
                          <td>
                            <strong>{formatAmount(payment.paymentAmount)}</strong>
                          </td>
                          <td>
                            <span className={statusInfo.className}>
                              {statusInfo.label}
                            </span>
                          </td>
                          <td>
                            <button
                              className="btn btn-sm btn-outline-primary"
                              onClick={() => {
                                console.log('Payment details:', payment);
                                alert(`Payment details:\n\nOrder ID: ${payment.orderId}\nСумма: ${formatAmount(payment.paymentAmount)}\nStatus: ${statusInfo.label}`);
                              }}
                              title="Show details"
                            >
                              <i className="bi bi-info-circle"></i>
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </>
      )}

      {payments.length > 0 && (
        <div className="card mt-4">
          <div className="card-header">
            <h5 className="mb-0">Payment statistics</h5>
          </div>
          <div className="card-body">
            <div className="row">
              <div className="col-md-2">
                <div className="card bg-light">
                  <div className="card-body text-center">
                    <h6 className="card-subtitle mb-1">Total</h6>
                    <h4 className="card-title">{payments.length}</h4>
                  </div>
                </div>
              </div>
              <div className="col-md-2">
                <div className="card bg-success text-white">
                  <div className="card-body text-center">
                    <h6 className="card-subtitle mb-1">Completed</h6>
                    <h4 className="card-title">
                      {payments.filter(p => p.status === PaymentStatus.COMPLETED).length}
                    </h4>
                  </div>
                </div>
              </div>
              <div className="col-md-2">
                <div className="card bg-danger text-white">
                  <div className="card-body text-center">
                    <h6 className="card-subtitle mb-1">FAILED</h6>
                    <h4 className="card-title">
                      {payments.filter(p => p.status === PaymentStatus.FAILED).length}
                    </h4>
                  </div>
                </div>
              </div>
              <div className="col-md-2">
                <div className="card bg-warning">
                  <div className="card-body text-center">
                    <h6 className="card-subtitle mb-1">PENDING</h6>
                    <h4 className="card-title">
                      {payments.filter(p => p.status === PaymentStatus.PENDING).length}
                    </h4>
                  </div>
                </div>
              </div>
              <div className="col-md-2">
                <div className="card bg-info text-white">
                  <div className="card-body text-center">
                    <h6 className="card-subtitle mb-1">REFUNDED</h6>
                    <h4 className="card-title">
                      {payments.filter(p => p.status === PaymentStatus.REFUNDED).length}
                    </h4>
                  </div>
                </div>
              </div>
              <div className="col-md-2">
                <div className="card bg-secondary text-white">
                  <div className="card-body text-center">
                    <h6 className="card-subtitle mb-1">CANCELLED</h6>
                    <h4 className="card-title">
                      {payments.filter(p => p.status === PaymentStatus.CANCELLED).length}
                    </h4>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PaymentsPage;