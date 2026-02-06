import React from 'react';
import { Container, Navbar, Nav, Button } from 'react-bootstrap';
import { Outlet, useNavigate, Link } from 'react-router-dom';
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
            <Navbar.Brand as={Link} to="/">INNOWISE STORE</Navbar.Brand>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse id="basic-navbar-nav">
              <Nav className="me-auto">
                <Link to="/" className="nav-link">MAIN</Link>
                <Link to="/orders" className="nav-link">ORDERS</Link>
                <Link to="/payments" className="nav-link">
                  <i className="bi bi-credit-card me-1"></i>
                  PAYMENTS
                </Link>
                {}
              </Nav>
              <Nav>
                {user && (
                    <Navbar.Text className="me-3">
                      Hello, {user.username}!
                    </Navbar.Text>
                )}
                <Button variant="outline-light" onClick={handleLogout}>
                  EXIT
                </Button>
              </Nav>
            </Navbar.Collapse>
          </Container>
        </Navbar>

        <Container className="mt-4">
          <Outlet /> {}
        </Container>
      </>
  );
};

export default Layout;