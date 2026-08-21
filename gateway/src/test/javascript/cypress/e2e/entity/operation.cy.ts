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

describe('Operation e2e test', () => {
  const operationPageUrl = '/operation';
  let username: string;
  let password: string;
  // const operationSample = {"type":"SELL","operationDate":"2023-12-22","quantity":7689.94,"price":17172.23,"amount":30531.95,"currency":"interviewer yawningly capitalise"};

  let operation;
  // let brokerAccount;
  // let asset;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/broker-accounts',
      body: {"externalAccountId":"as","displayName":"warlike"},
    }).then(({ body }) => {
      brokerAccount = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/assets',
      body: {"ticket":"yum next","category":"unto","country":"Liberia","description":"overvalue frugal meanwhile"},
    }).then(({ body }) => {
      asset = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/operations+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/operations').as('postEntityRequest');
    cy.intercept('DELETE', '/api/operations/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/broker-accounts', {
      statusCode: 200,
      body: [brokerAccount],
    });

    cy.intercept('GET', '/api/assets', {
      statusCode: 200,
      body: [asset],
    });

    cy.intercept('GET', '/api/operations', {
      statusCode: 200,
      body: [],
    });

  });
   */

  afterEach(() => {
    if (operation) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/operations/${operation.id}`,
      }).then(() => {
        operation = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
    if (brokerAccount) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/broker-accounts/${brokerAccount.id}`,
      }).then(() => {
        brokerAccount = undefined;
      });
    }
    if (asset) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/assets/${asset.id}`,
      }).then(() => {
        asset = undefined;
      });
    }
  });
   */

  it('Operations menu should load Operations page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('operation');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Operation').should('exist');
    cy.location('pathname').should('eq', operationPageUrl);
  });

  describe('Operation page', () => {
    it('should have translated page title', () => {
      cy.visit(operationPageUrl);
      cy.getEntityHeading('Operation').should('not.contain', 'gatewayApp.operation.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(operationPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Operation page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${operationPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Operation');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', operationPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/operations',
          body: {
            ...operationSample,
            account: brokerAccount,
            asset: asset,
          },
        }).then(({ body }) => {
          operation = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/operations+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/operations?page=0&size=20>; rel="last",<http://localhost/api/operations?page=0&size=20>; rel="first"',
              },
              body: [operation],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(operationPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(operationPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Operation page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('operation');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', operationPageUrl);
      });

      it('edit button click should load edit Operation page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Operation');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', operationPageUrl);
      });

      it('edit button click should load edit Operation page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Operation');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', operationPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Operation', () => {
        cy.intercept('GET', '/api/operations/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('operation').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', operationPageUrl);

        operation = undefined;
      });
    });
  });

  describe('new Operation page', () => {
    beforeEach(() => {
      cy.visit(operationPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Operation');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Operation', () => {
      cy.get(`[data-cy="type"]`).select('BUY');

      cy.get(`[data-cy="operationDate"]`).type('2023-12-22');
      cy.get(`[data-cy="operationDate"]`).blur();
      cy.get(`[data-cy="operationDate"]`).should('have.value', '2023-12-22');

      cy.get(`[data-cy="quantity"]`).type('31300.5');
      cy.get(`[data-cy="quantity"]`).should('have.value', '31300.5');

      cy.get(`[data-cy="price"]`).type('31711.94');
      cy.get(`[data-cy="price"]`).should('have.value', '31711.94');

      cy.get(`[data-cy="amount"]`).type('25513.38');
      cy.get(`[data-cy="amount"]`).should('have.value', '25513.38');

      cy.get(`[data-cy="currency"]`).type('near by');
      cy.get(`[data-cy="currency"]`).should('have.value', 'near by');

      cy.get(`[data-cy="underlyingPrice"]`).type('1292.36');
      cy.get(`[data-cy="underlyingPrice"]`).should('have.value', '1292.36');

      cy.get(`[data-cy="commission"]`).type('28850.33');
      cy.get(`[data-cy="commission"]`).should('have.value', '28850.33');

      cy.get(`[data-cy="account"]`).select(1);
      cy.get(`[data-cy="asset"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        operation = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', operationPageUrl);
    });
  });
});
