import React from 'react';
import { Card, Row, Col } from 'react-bootstrap';
import { useAuth } from '../contexts/AuthContext';

const Home = () => {
  const { user } = useAuth();

  return (
      <>
        <h1 className="mb-4">WELCOME</h1>

        <Row className="mb-4">
          <Col>
            <Card>
              <Card.Body>
                <Card.Title>Information about user</Card.Title>
                <Card.Text>
                  <strong>User name:</strong> {user?.username}<br />
                  <strong>Email:</strong> {user?.email}<br />
                  <strong>Roles:</strong> {user?.roles?.join(', ') || 'No role'}
                </Card.Text>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </>
  );
};

export default Home;