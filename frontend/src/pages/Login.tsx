import React, { useState } from 'react';
import { Form, Button, Container, Card, Alert, Row, Col } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authApi, LoginRequest } from '../api/client';

const Login = () => {
  const [formData, setFormData] = useState<LoginRequest>({
    login: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await authApi.login(formData);
      const responseData = response.data;

      console.log('API Login Response:', responseData);

      const accessToken = responseData.accessToken;
      const refreshToken = responseData.refreshToken;

      if (!accessToken) {
        setError('No accessToken');
        return;
      }

      localStorage.setItem('token', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      const userData = {
        username: formData.login,
        email: responseData.email || '',
        roles: responseData.roles || ['USER'],
      };

      localStorage.setItem('user', JSON.stringify(userData));

      login(accessToken, userData);
      navigate('/');

    } catch (err: any) {
      console.error('Login error:', err);

      if (err.response?.status === 401) {
        setError('Incorrect login or password');
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response) {
        setError(`Server error: ${err.response.status}`);
      } else {
        setError('Unknown error');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
      <Container className="mt-5">
        <Row className="justify-content-center">
          <Col md={6} lg={4}>
            <Card>
              <Card.Body>
                <Card.Title className="text-center mb-2">`INNOWISE-STORE`</Card.Title>
                <Card.Title className="text-center mb-4">Log in system</Card.Title>

                {error && (
                    <Alert variant="danger" dismissible onClose={() => setError('')}>
                      {error.split('\n').map((line, i) => (
                          <div key={i}>{line}</div>
                      ))}
                    </Alert>
                )}

                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-3">
                    <Form.Label>Login</Form.Label>
                    <Form.Control
                        type="text"
                        name="login"
                        value={formData.login}
                        onChange={handleChange}
                        placeholder="Enter login"
                        required
                        disabled={loading}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Password</Form.Label>
                    <Form.Control
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="Enter password"
                        required
                        disabled={loading}
                    />
                  </Form.Group>

                  <Button
                      variant="primary"
                      type="submit"
                      className="w-100 mb-3"
                      disabled={loading}
                  >
                    {loading ? 'Entrance..' : 'Log in'}
                  </Button>
                </Form>

                <div className="text-center mt-3">
                  <p>
                    Don`t have account?{' '}
                    <Link to="/register" className="text-decoration-none">
                      Register
                    </Link>
                  </p>
                </div>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
  );
};

export default Login;