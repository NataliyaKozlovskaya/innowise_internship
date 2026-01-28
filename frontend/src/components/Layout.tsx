import React from 'react';
import { Container, Navbar, Nav, Button } from 'react-bootstrap';
import { Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Layout = () => {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
      <>
        <Navbar bg="dark" variant="dark" expand="lg">
          <Container>
            <Navbar.Brand href="/">Микросервисное приложение</Navbar.Brand>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse id="basic-navbar-nav">
              <Nav className="me-auto">
                <Nav.Link href="/">Главная</Nav.Link>
                {/* Добавьте другие ссылки здесь позже */}
              </Nav>
              <Nav>
                {user && (
                    <Navbar.Text className="me-3">
                      Привет, {user.username}!
                    </Navbar.Text>
                )}
                <Button variant="outline-light" onClick={handleLogout}>
                  Выйти
                </Button>
              </Nav>
            </Navbar.Collapse>
          </Container>
        </Navbar>

        <Container className="mt-4">
          <Outlet /> {/* Здесь будут отображаться дочерние компоненты */}
        </Container>
      </>
  );
};

export default Layout;