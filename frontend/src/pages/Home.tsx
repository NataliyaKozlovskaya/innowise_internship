import React from 'react';
import { Card, Row, Col } from 'react-bootstrap';
import { useAuth } from '../contexts/AuthContext';

const Home = () => {
  const { user } = useAuth();

  return (
      <>
        <h1 className="mb-4">Добро пожаловать!</h1>

        <Row className="mb-4">
          <Col>
            <Card>
              <Card.Body>
                <Card.Title>Информация о пользователе</Card.Title>
                <Card.Text>
                  <strong>Имя пользователя:</strong> {user?.username}<br />
                  <strong>Email:</strong> {user?.email}<br />
                  <strong>Роли:</strong> {user?.roles?.join(', ') || 'Нет ролей'}
                </Card.Text>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        <Row>
          <Col md={4}>
            <Card className="h-100">
              <Card.Body>
                <Card.Title>Заказы</Card.Title>
                <Card.Text>
                  Управление заказами. Создавайте, просматривайте и отслеживайте статус заказов.
                </Card.Text>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="h-100">
              <Card.Body>
                <Card.Title>Платежи</Card.Title>
                <Card.Text>
                  Оплата заказов и просмотр истории платежей.
                </Card.Text>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="h-100">
              <Card.Body>
                <Card.Title>Профиль</Card.Title>
                <Card.Text>
                  Управление вашим профилем и настройками аккаунта.
                </Card.Text>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </>
  );
};

export default Home;