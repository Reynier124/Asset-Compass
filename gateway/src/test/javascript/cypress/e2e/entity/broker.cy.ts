import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('Broker e2e test', () => {
  const brokerPageUrl = '/broker';
  let username: string;
  let password: string;
  const brokerSample = { name: 'poor that yawningly' };

  let broker;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/brokers+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/brokers').as('postEntityRequest');
    cy.intercept('DELETE', '/api/brokers/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (broker) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/brokers/${broker.id}`,
      }).then(() => {
        broker = undefined;
      });
    }
  });

  it('Brokers menu should load Brokers page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('broker');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Broker').should('exist');
    cy.location('pathname').should('eq', brokerPageUrl);
  });

  describe('Broker page', () => {
    it('should have translated page title', () => {
      cy.visit(brokerPageUrl);
      cy.getEntityHeading('Broker').should('not.contain', 'gatewayApp.broker.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(brokerPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Broker page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${brokerPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Broker');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/brokers',
          body: brokerSample,
        }).then(({ body }) => {
          broker = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/brokers+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [broker],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(brokerPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Broker page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('broker');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerPageUrl);
      });

      it('edit button click should load edit Broker page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Broker');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerPageUrl);
      });

      it('edit button click should load edit Broker page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Broker');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerPageUrl);
      });

      it('last delete button click should delete instance of Broker', () => {
        cy.intercept('GET', '/api/brokers/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('broker').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerPageUrl);

        broker = undefined;
      });
    });
  });

  describe('new Broker page', () => {
    beforeEach(() => {
      cy.visit(brokerPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Broker');
    });

    it('should create an instance of Broker', () => {
      cy.get(`[data-cy="name"]`).type('torn lovingly than');
      cy.get(`[data-cy="name"]`).should('have.value', 'torn lovingly than');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        broker = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', brokerPageUrl);
    });
  });
});
