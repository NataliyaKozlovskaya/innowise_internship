import React, { useState } from 'react';
import { Form, Button, Container, Card, Alert, Row, Col } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authApi, LoginRequest } from '../api/client';

const Login = () => {
  // ИЗМЕНИЛ: login вместо username
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

      // Логируем ответ для отладки
      console.log('API Login Response:', responseData);

      // У вас точно есть accessToken и refreshToken
      const accessToken = responseData.accessToken;
      const refreshToken = responseData.refreshToken;

      if (!accessToken) {
        setError('Сервер не вернул accessToken');
        return;
      }

      // Сохраняем оба токена
      localStorage.setItem('token', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      // Создаём объект пользователя (можно получить через отдельный запрос к /users/me)
      const userData = {
        username: formData.login, // Используем login как username
        email: '', // Нужно получить отдельным запросом
        roles: ['USER'], // Нужно получить отдельным запросом
      };

      localStorage.setItem('user', JSON.stringify(userData));

      // Обновляем контекст
      login(accessToken, userData);
      navigate('/');

    } catch (err: any) {
      console.error('Login error:', err);

      if (err.response?.status === 401) {
        setError('Неверный логин или пароль');
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.message?.includes('Network Error')) {
        setError('Не удалось подключиться к серверу. Проверьте:');
        setError(prev => prev + '\n• Запущен ли API Gateway? (порт 8093)');
        setError(prev => prev + '\n• Правильный ли URL: ' + process.env.REACT_APP_API_URL);
      } else if (err.response) {
        setError(`Ошибка сервера: ${err.response.status}`);
      } else {
        setError('Произошла неизвестная ошибка');
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
                <Card.Title className="text-center mb-4">Вход в систему</Card.Title>

                {error && (
                    <Alert variant="danger" dismissible onClose={() => setError('')}>
                      {error.split('\n').map((line, i) => (
                          <div key={i}>{line}</div>
                      ))}
                    </Alert>
                )}

                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-3">
                    <Form.Label>Логин</Form.Label> {/* ИЗМЕНИЛ: Логин вместо Имя пользователя */}
                    <Form.Control
                        type="text"
                        name="login"
                        value={formData.login}
                        onChange={handleChange}
                        placeholder="Введите логин"
                        required
                        disabled={loading}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Пароль</Form.Label>
                    <Form.Control
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="Введите пароль"
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
                    {loading ? 'Вход...' : 'Войти'}
                  </Button>
                </Form>

                <div className="text-center mt-3">
                  <p>
                    Нет аккаунта?{' '}
                    <Link to="/register" className="text-decoration-none">
                      Зарегистрируйтесь
                    </Link>
                  </p>
                </div>

                {/* Отладочная информация */}
                {/*<div className="mt-4 p-3 bg-light border rounded small">*/}
                {/*  <h6 className="mb-2">Отладка:</h6>*/}
                {/*  <p className="mb-1">*/}
                {/*    <strong>Отправляемый запрос:</strong><br />*/}
                {/*    POST {process.env.REACT_APP_API_URL}/auth/login<br />*/}
                {/*    {JSON.stringify(formData, null, 2)}*/}
                {/*  </p>*/}
                {/*  <p className="mb-1">*/}
                {/*    <strong>API URL:</strong> {process.env.REACT_APP_API_URL}*/}
                {/*  </p>*/}
                {/*  <p className="mb-1">*/}
                {/*    <strong>Токен:</strong> {localStorage.getItem('token') ? 'Есть' : 'Нет'}*/}
                {/*  </p>*/}
                {/*  <button*/}
                {/*      className="btn btn-sm btn-outline-secondary mt-1"*/}
                {/*      onClick={() => {*/}
                {/*        console.log('LocalStorage:', localStorage);*/}
                {/*        console.log('Form data:', formData);*/}
                {/*      }}*/}
                {/*  >*/}
                {/*    Лог в консоль*/}
                {/*  </button>*/}
                {/*</div>*/}
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
  );
};

export default Login;