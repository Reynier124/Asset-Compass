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

describe('TaxEvent e2e test', () => {
  const taxEventPageUrl = '/tax-event';
  let username: string;
  let password: string;
  // const taxEventSample = {"type":"RETENCION_DIVIDENDO","taxDate":"2023-12-22","amount":31274.97,"currency":"correctly carelessly boo"};

  let taxEvent;
  // let brokerAccount;

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
      body: {"externalAccountId":"excepting","displayName":"factorize once"},
    }).then(({ body }) => {
      brokerAccount = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/tax-events+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/tax-events').as('postEntityRequest');
    cy.intercept('DELETE', '/api/tax-events/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/broker-accounts', {
      statusCode: 200,
      body: [brokerAccount],
    });

    cy.intercept('GET', '/api/operations', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/income-events', {
      statusCode: 200,
      body: [],
    });

  });
   */

  afterEach(() => {
    if (taxEvent) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/tax-events/${taxEvent.id}`,
      }).then(() => {
        taxEvent = undefined;
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
  });
   */

  it('TaxEvents menu should load TaxEvents page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('tax-event');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('TaxEvent').should('exist');
    cy.location('pathname').should('eq', taxEventPageUrl);
  });

  describe('TaxEvent page', () => {
    it('should have translated page title', () => {
      cy.visit(taxEventPageUrl);
      cy.getEntityHeading('TaxEvent').should('not.contain', 'gatewayApp.taxEvent.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(taxEventPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create TaxEvent page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${taxEventPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('TaxEvent');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', taxEventPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/tax-events',
          body: {
            ...taxEventSample,
            account: brokerAccount,
          },
        }).then(({ body }) => {
          taxEvent = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/tax-events+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/tax-events?page=0&size=20>; rel="last",<http://localhost/api/tax-events?page=0&size=20>; rel="first"',
              },
              body: [taxEvent],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(taxEventPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(taxEventPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details TaxEvent page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('taxEvent');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', taxEventPageUrl);
      });

      it('edit button click should load edit TaxEvent page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('TaxEvent');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', taxEventPageUrl);
      });

      it('edit button click should load edit TaxEvent page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('TaxEvent');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', taxEventPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of TaxEvent', () => {
        cy.intercept('GET', '/api/tax-events/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('taxEvent').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', taxEventPageUrl);

        taxEvent = undefined;
      });
    });
  });

  describe('new TaxEvent page', () => {
    beforeEach(() => {
      cy.visit(taxEventPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('TaxEvent');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of TaxEvent', () => {
      cy.get(`[data-cy="type"]`).select('IVA');

      cy.get(`[data-cy="taxDate"]`).type('2023-12-22');
      cy.get(`[data-cy="taxDate"]`).blur();
      cy.get(`[data-cy="taxDate"]`).should('have.value', '2023-12-22');

      cy.get(`[data-cy="amount"]`).type('17764.11');
      cy.get(`[data-cy="amount"]`).should('have.value', '17764.11');

      cy.get(`[data-cy="currency"]`).type('factorize');
      cy.get(`[data-cy="currency"]`).should('have.value', 'factorize');

      cy.get(`[data-cy="account"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        taxEvent = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', taxEventPageUrl);
    });
  });
});
