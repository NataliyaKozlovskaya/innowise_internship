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

    // Валидация
    if (formData.password !== formData.confirmPassword) {
      setError('Пароли не совпадают');
      return;
    }

    if (formData.password.length < 6) {
      setError('Пароль должен быть не менее 6 символов');
      return;
    }

    if (!formData.login.trim()) {
      setError('Логин обязателен');
      return;
    }

    if (!formData.email.trim()) {
      setError('Email обязателен');
      return;
    }

    setLoading(true);

    try {
      // Отправляем ВСЕ данные в одну конечную точку
      // API Gateway сам разберется куда что отправлять
      const response = await authApi.register({
        login: formData.login,
        password: formData.password,
        email: formData.email,
        name: formData.name,
        surname: formData.surname,
        birthDate: formData.birthDate,
      });

      console.log('Registration response:', response.data);

      // После регистрации пробуем автоматически войти
      try {
        const loginResponse = await authApi.login({
          login: formData.login,
          password: formData.password
        });

        const { accessToken, refreshToken } = loginResponse.data;

        // Сохраняем токены
        localStorage.setItem('token', accessToken);
        if (refreshToken) {
          localStorage.setItem('refreshToken', refreshToken);
        }

        // Сохраняем данные пользователя
        const userData = {
          username: formData.login,
          email: formData.email,
          name: formData.name,
          surname: formData.surname,
          roles: ['USER'],
        };

        localStorage.setItem('user', JSON.stringify(userData));
        login(accessToken, userData);

        setSuccess('Регистрация и вход выполнены! Перенаправление...');

        setTimeout(() => {
          navigate('/');
        }, 1500);

      } catch (loginErr) {
        // Если авто-вход не удался, просто редиректим на логин
        setSuccess('Регистрация успешна! Теперь вы можете войти.');

        setTimeout(() => {
          navigate('/login');
        }, 3000);
      }

    } catch (err: any) {
      console.error('Registration error:', err);

      if (err.response?.status === 400) {
        setError(err.response.data.message || 'Ошибка регистрации');
      } else if (err.response?.data?.error) {
        setError(err.response.data.error);
      } else if (err.message?.includes('Network Error')) {
        setError('Не удалось подключиться к серверу');
      } else {
        setError('Произошла ошибка при регистрации');
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
                <Card.Title className="text-center mb-3">Регистрация</Card.Title>

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
                        <Form.Label>Имя *</Form.Label>
                        <Form.Control
                            type="text"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            placeholder="Ваше имя"
                            required
                            disabled={loading}
                        />
                      </Form.Group>
                    </Col>

                    <Col md={6}>
                      <Form.Group className="mb-2">
                        <Form.Label>Фамилия *</Form.Label>
                        <Form.Control
                            type="text"
                            name="surname"
                            value={formData.surname}
                            onChange={handleChange}
                            placeholder="Ваша фамилия"
                            required
                            disabled={loading}
                        />
                      </Form.Group>
                    </Col>
                  </Row>

                  <Form.Group className="mb-2">
                    <Form.Label>Логин *</Form.Label>
                    <Form.Control
                        type="text"
                        name="login"
                        value={formData.login}
                        onChange={handleChange}
                        placeholder="Придумайте логин"
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
                    <Form.Label>Дата рождения</Form.Label>
                    <Form.Control
                        type="date"
                        name="birthDate"
                        value={formData.birthDate}
                        onChange={handleChange}
                        max={new Date().toISOString().split('T')[0]}
                        disabled={loading}
                    />
                    <Form.Text className="text-muted">
                      Необязательно
                    </Form.Text>
                  </Form.Group>

                  <Form.Group className="mb-2">
                    <Form.Label>Пароль *</Form.Label>
                    <Form.Control
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="Придумайте пароль (мин. 6 символов)"
                        required
                        disabled={loading}
                    />
                  </Form.Group>

                  <Form.Group className="mb-2">
                    <Form.Label>Подтвердите пароль *</Form.Label>
                    <Form.Control
                        type="password"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})}
                        placeholder="Повторите пароль"
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
                      {loading ? 'Регистрация...' : 'Зарегистрироваться'}
                    </Button>

                    <div className="text-center mt-3">
                      <Link to="/login">Уже есть аккаунт? Войдите</Link>
                    </div>
                  </div>
                </Form>

                {/* Отладочная информация */}
                {/*<div className="mt-4 p-2 bg-light border rounded small">*/}
                {/*  <p className="mb-1">*/}
                {/*    <strong>Отправляемые данные:</strong>*/}
                {/*  </p>*/}
                {/*  <pre className="mb-0" style={{fontSize: '11px'}}>*/}
                {/*  {JSON.stringify({*/}
                {/*    login: formData.login,*/}
                {/*    email: formData.email,*/}
                {/*    name: formData.name,*/}
                {/*    surname: formData.surname,*/}
                {/*    birthDate: formData.birthDate || '(не указано)',*/}
                {/*    password: '***' // не показываем пароль*/}
                {/*  }, null, 2)}*/}
                {/*</pre>*/}
                {/*</div>*/}
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
  );
};

export default Register;