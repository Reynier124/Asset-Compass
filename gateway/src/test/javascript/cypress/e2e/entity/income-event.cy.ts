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

describe('IncomeEvent e2e test', () => {
  const incomeEventPageUrl = '/income-event';
  let username: string;
  let password: string;
  // const incomeEventSample = {"type":"DIVIDENDO","eventDate":"2023-12-22","amount":16545.73,"currency":"collaboration"};

  let incomeEvent;
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
      body: {"externalAccountId":"certainly","displayName":"whereas"},
    }).then(({ body }) => {
      brokerAccount = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/assets',
      body: {"ticket":"uh-huh","category":"instruction","country":"Bouvet Island","description":"immediately as second"},
    }).then(({ body }) => {
      asset = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/income-events+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/income-events').as('postEntityRequest');
    cy.intercept('DELETE', '/api/income-events/*').as('deleteEntityRequest');
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

  });
   */

  afterEach(() => {
    if (incomeEvent) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/income-events/${incomeEvent.id}`,
      }).then(() => {
        incomeEvent = undefined;
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

  it('IncomeEvents menu should load IncomeEvents page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('income-event');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('IncomeEvent').should('exist');
    cy.location('pathname').should('eq', incomeEventPageUrl);
  });

  describe('IncomeEvent page', () => {
    it('should have translated page title', () => {
      cy.visit(incomeEventPageUrl);
      cy.getEntityHeading('IncomeEvent').should('not.contain', 'gatewayApp.incomeEvent.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(incomeEventPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create IncomeEvent page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${incomeEventPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('IncomeEvent');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', incomeEventPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/income-events',
          body: {
            ...incomeEventSample,
            account: brokerAccount,
            asset: asset,
          },
        }).then(({ body }) => {
          incomeEvent = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/income-events+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/income-events?page=0&size=20>; rel="last",<http://localhost/api/income-events?page=0&size=20>; rel="first"',
              },
              body: [incomeEvent],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(incomeEventPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(incomeEventPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details IncomeEvent page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('incomeEvent');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', incomeEventPageUrl);
      });

      it('edit button click should load edit IncomeEvent page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('IncomeEvent');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', incomeEventPageUrl);
      });

      it('edit button click should load edit IncomeEvent page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('IncomeEvent');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', incomeEventPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of IncomeEvent', () => {
        cy.intercept('GET', '/api/income-events/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('incomeEvent').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', incomeEventPageUrl);

        incomeEvent = undefined;
      });
    });
  });

  describe('new IncomeEvent page', () => {
    beforeEach(() => {
      cy.visit(incomeEventPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('IncomeEvent');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of IncomeEvent', () => {
      cy.get(`[data-cy="type"]`).select('DIVIDENDO');

      cy.get(`[data-cy="eventDate"]`).type('2023-12-22');
      cy.get(`[data-cy="eventDate"]`).blur();
      cy.get(`[data-cy="eventDate"]`).should('have.value', '2023-12-22');

      cy.get(`[data-cy="amount"]`).type('11753.95');
      cy.get(`[data-cy="amount"]`).should('have.value', '11753.95');

      cy.get(`[data-cy="currency"]`).type('towards surprised');
      cy.get(`[data-cy="currency"]`).should('have.value', 'towards surprised');

      cy.get(`[data-cy="account"]`).select(1);
      cy.get(`[data-cy="asset"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        incomeEvent = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', incomeEventPageUrl);
    });
  });
});
