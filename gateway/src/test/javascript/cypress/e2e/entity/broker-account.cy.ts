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

describe('BrokerAccount e2e test', () => {
  const brokerAccountPageUrl = '/broker-account';
  let username: string;
  let password: string;
  const brokerAccountSample = { externalAccountId: 'since sew than' };

  let brokerAccount;
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
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/brokers',
      body: { name: 'apropos' },
    }).then(({ body }) => {
      broker = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/broker-accounts+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/broker-accounts').as('postEntityRequest');
    cy.intercept('DELETE', '/api/broker-accounts/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/brokers', {
      statusCode: 200,
      body: [broker],
    });
  });

  afterEach(() => {
    if (brokerAccount) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/broker-accounts/${brokerAccount.id}`,
      }).then(() => {
        brokerAccount = undefined;
      });
    }
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

  it('BrokerAccounts menu should load BrokerAccounts page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('broker-account');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('BrokerAccount').should('exist');
    cy.location('pathname').should('eq', brokerAccountPageUrl);
  });

  describe('BrokerAccount page', () => {
    it('should have translated page title', () => {
      cy.visit(brokerAccountPageUrl);
      cy.getEntityHeading('BrokerAccount').should('not.contain', 'gatewayApp.brokerAccount.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(brokerAccountPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create BrokerAccount page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${brokerAccountPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('BrokerAccount');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerAccountPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/broker-accounts',
          body: {
            ...brokerAccountSample,
            broker,
          },
        }).then(({ body }) => {
          brokerAccount = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/broker-accounts+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [brokerAccount],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(brokerAccountPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details BrokerAccount page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('brokerAccount');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerAccountPageUrl);
      });

      it('edit button click should load edit BrokerAccount page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('BrokerAccount');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerAccountPageUrl);
      });

      it('edit button click should load edit BrokerAccount page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('BrokerAccount');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerAccountPageUrl);
      });

      it('last delete button click should delete instance of BrokerAccount', () => {
        cy.intercept('GET', '/api/broker-accounts/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('brokerAccount').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', brokerAccountPageUrl);

        brokerAccount = undefined;
      });
    });
  });

  describe('new BrokerAccount page', () => {
    beforeEach(() => {
      cy.visit(brokerAccountPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('BrokerAccount');
    });

    it('should create an instance of BrokerAccount', () => {
      cy.get(`[data-cy="externalAccountId"]`).type('opposite hospitable');
      cy.get(`[data-cy="externalAccountId"]`).should('have.value', 'opposite hospitable');

      cy.get(`[data-cy="displayName"]`).type('upon yowza ack');
      cy.get(`[data-cy="displayName"]`).should('have.value', 'upon yowza ack');

      cy.get(`[data-cy="broker"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        brokerAccount = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', brokerAccountPageUrl);
    });
  });
});
