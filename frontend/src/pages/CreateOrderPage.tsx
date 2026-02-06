import React, { useState } from 'react';
import { Form, Button, Container, Card, Alert, Row, Col, Table } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { orderApi, CreateOrderRequest, OrderItemRequest } from '../api/client';
import { useAuth } from '../contexts/AuthContext';

const CreateOrderPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [items, setItems] = useState<OrderItemRequest[]>([
    { itemId: 0, quantity: 1 }
  ]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleItemChange = (index: number, field: keyof OrderItemRequest, value: any) => {
    const newItems = [...items];
    newItems[index] = { ...newItems[index], [field]: value };
    setItems(newItems);
  };

  const addItem = () => {
    setItems([...items, { itemId: 0, quantity: 1 }]);
  };

  const removeItem = (index: number) => {
    if (items.length > 1) {
      const newItems = items.filter((_, i) => i !== index);
      setItems(newItems);
    }
  };

  const handleCreateOrder = async () => {
    setError('');
    setLoading(true);

    try {
      const userId = localStorage.getItem('uuid') || '';

      const orderData: CreateOrderRequest = {
        userId: userId,
        items: items
      };

      console.log('Order creation:', orderData);
      const response = await orderApi.createOrder(orderData);

      console.log('Server response:', response);
      alert('Order create successfully!');
      setItems([{ itemId: 0, quantity: 1 }]);

    } catch (err: any) {
      console.error('Error:', err);
      console.error('Error details:', err.response?.data);


      const errorMessage = err.response?.data?.message ||
                          err.response?.data?.error ||
                          'Error while order create';
      setError(errorMessage);

    } finally {
      setLoading(false);
    }
  };

  const addExampleItems = () => {
    setItems([
      { itemId: 5, quantity: 10 },
      { itemId: 3, quantity: 12 }
    ]);
    setError('');
  };

  return (
    <Container className="mt-4">
      <Row className="mb-4">
        <Col>
          <h2>Создание заказа</h2>
          <p className="text-muted">Add items and push "Create order"</p>
        </Col>
      </Row>

      {error && <Alert variant="danger">{error}</Alert>}

      <Card className="mb-3">
        <Card.Body>
          <h5>Fast act:</h5>
          <div className="d-flex gap-2 mb-3">
            <Button variant="outline-primary" onClick={addExampleItems}>
              Fill in example (ID: 5 и 3)
            </Button>
            <Button variant="outline-secondary" onClick={() => setItems([{ itemId: 0, quantity: 1 }])}>
              Clear the form
            </Button>
          </div>
        </Card.Body>
      </Card>

      <Card className="mb-4">
        <Card.Body>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5>Items in order</h5>
            <Button variant="outline-primary" onClick={addItem} size="sm">
              + Add item
            </Button>
          </div>

          <Table responsive>
            <thead>
              <tr>
                <th>Item Id</th>
                <th>Quantity</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item, index) => (
                <tr key={index}>
                  <td>
                    <Form.Control
                      type="number"
                      value={item.itemId || ''}
                      onChange={(e) => handleItemChange(index, 'itemId', parseInt(e.target.value) || 0)}
                      placeholder="Example: 5"
                      min="1"
                    />
                  </td>
                  <td>
                    <Form.Control
                      type="number"
                      value={item.quantity}
                      onChange={(e) => handleItemChange(index, 'quantity', parseInt(e.target.value) || 1)}
                      placeholder="Quantity"
                      min="1"
                    />
                  </td>
                  <td>
                    {items.length > 1 && (
                      <Button
                        variant="outline-danger"
                        size="sm"
                        onClick={() => removeItem(index)}
                      >
                        Delete
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      {}
      <Card>
        <Card.Body className="text-center">
          <h5 className="mb-3">Ready for order creation?</h5>

          <Button
            variant="success"
            size="lg"
            onClick={handleCreateOrder}
            disabled={loading}
            className="mb-3"
            style={{ fontSize: '1.2rem', padding: '15px 30px' }}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2"></span>
                Order creation...
              </>
            ) : (
              '✅ CREATE ORDER'
            )}
          </Button>

        </Card.Body>
      </Card>

      <div className="mt-4">
        <Button variant="outline-secondary" onClick={() => navigate('/')}>
          ← MAIN
        </Button>
      </div>
    </Container>
  );
};

export default CreateOrderPage;