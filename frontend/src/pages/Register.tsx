import React, { useState } from 'react';
import { Form, Button, Container, Card, Alert, Row, Col } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authApi } from '../api/client';

const Register = () => {
  const [formData, setFormData] = useState({
    login: '',
    email: '',
    password: '',
    confirmPassword: '',
    name: '',
    surname: '',
    birthDate: '',
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
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
    setSuccess('');

    if (formData.password !== formData.confirmPassword) {
      setError('The passwords do not match');
      return;
    }

    if (formData.password.length < 6) {
      setError('The password must be at least 6 characters long.');
      return;
    }

    if (!formData.login.trim()) {
      setError('Login is required');
      return;
    }

    if (!formData.email.trim()) {
      setError('Email is required');
      return;
    }

    setLoading(true);

    try {
      const response = await authApi.register({
        login: formData.login,
        password: formData.password,
        email: formData.email,
        name: formData.name,
        surname: formData.surname,
        birthDate: formData.birthDate,
      });

      console.log('Registration response:', response.data);

      const { uuid, login: registeredLogin, email: registeredEmail } = response.data;

      try {
        const loginResponse = await authApi.login({
          login: formData.login,
          password: formData.password
        });

        const { accessToken, refreshToken } = loginResponse.data;

        localStorage.setItem('token', accessToken);
        if (refreshToken) {
          localStorage.setItem('refreshToken', refreshToken);
        }

        const userData = {
          username: registeredLogin || formData.login,
          email: registeredEmail || formData.email,
          name: formData.name,
          surname: formData.surname,
          roles: ['USER'],
        };

        localStorage.setItem('uuid', uuid);

        localStorage.setItem('user', JSON.stringify(userData));

        login(accessToken, userData);

        setSuccess('Registration and login completed! Redirect...');

        setTimeout(() => {
          navigate('/');
        }, 1500);

      } catch (loginErr) {
        localStorage.setItem('uuid', uuid);
        console.log('UUID saved:', uuid);

        setSuccess('Registration successful! You can now log in');

        setTimeout(() => {
          navigate('/login');
        }, 3000);
      }

    } catch (err: any) {
      console.error('Registration error:', err);

      if (err.response?.status === 400) {
        setError(err.response.data.message || 'Registration error');
      } else if (err.response?.data?.error) {
        setError(err.response.data.error);
      } else if (err.message?.includes('Network Error')) {
        setError('Failed to connect to the server');
      } else {
        setError('An error occurred while registering');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
      <Container className="mt-5">
        <Row className="justify-content-center">
          <Col md={8} lg={6}>
            <Card>
              <Card.Body>
                <Card.Title className="text-center mb-2">`INNOWISE-STORE`</Card.Title>
                <Card.Title className="text-center mb-3">Registration</Card.Title>

                {error && (
                    <Alert variant="danger" dismissible onClose={() => setError('')}>
                      {error}
                    </Alert>
                )}

                {success && (
                    <Alert variant="success" dismissible onClose={() => setSuccess('')}>
                      {success}
                    </Alert>
                )}

                <Form onSubmit={handleSubmit}>
                  <Row>
                    <Col md={6}>
                      <Form.Group className="mb-2">
                        <Form.Label>Name *</Form.Label>
                        <Form.Control
                            type="text"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            placeholder="Your name"
                            required
                            disabled={loading}
                        />
                      </Form.Group>
                    </Col>

                    <Col md={6}>
                      <Form.Group className="mb-2">
                        <Form.Label>Surname *</Form.Label>
                        <Form.Control
                            type="text"
                            name="surname"
                            value={formData.surname}
                            onChange={handleChange}
                            placeholder="Your surname"
                            required
                            disabled={loading}
                        />
                      </Form.Group>
                    </Col>
                  </Row>

                  <Form.Group className="mb-2">
                    <Form.Label>Login *</Form.Label>
                    <Form.Control
                        type="text"
                        name="login"
                        value={formData.login}
                        onChange={handleChange}
                        placeholder="Come up with a login"
                        required
                        disabled={loading}
                    />
                  </Form.Group>

                  <Form.Group className="mb-2">
                    <Form.Label>Email *</Form.Label>
                    <Form.Control
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="example@email.com"
                        required
                        disabled={loading}
                    />
                  </Form.Group>

                  <Form.Group className="mb-2">
                    <Form.Label>Date of birth</Form.Label>
                    <Form.Control
                        type="date"
                        name="birthDate"
                        value={formData.birthDate}
                        onChange={handleChange}
                        max={new Date().toISOString().split('T')[0]}
                        disabled={loading}
                    />
                    <Form.Text className="text-muted">
                      Not necessarily
                    </Form.Text>
                  </Form.Group>

                  <Form.Group className="mb-2">
                    <Form.Label>Password *</Form.Label>
                    <Form.Control
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="Create a password (min. 6 characters)"
                        required
                        disabled={loading}
                    />
                  </Form.Group>

                  <Form.Group className="mb-2">
                    <Form.Label>Confirm your password *</Form.Label>
                    <Form.Control
                        type="password"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})}
                        placeholder="Repeat password"
                        required
                        disabled={loading}
                    />
                  </Form.Group>

                  <div className="d-grid gap-2">
                    <Button
                        variant="primary"
                        type="submit"
                        disabled={loading}
                    >
                      {loading ? 'Registration...' : 'Register'}
                    </Button>

                    <div className="text-center mt-3">
                      <Link to="/login">Already have an account? Log in</Link>
                    </div>
                  </div>
                </Form>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
  );
};

export default Register;