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

describe('Rebate e2e test', () => {
  const rebatePageUrl = '/rebate';
  let username: string;
  let password: string;
  // const rebateSample = {"rebateDate":"2023-12-22","amount":26279.18,"currency":"lest weep lest"};

  let rebate;
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
      body: {"externalAccountId":"machine","displayName":"yahoo"},
    }).then(({ body }) => {
      brokerAccount = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/rebates+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/rebates').as('postEntityRequest');
    cy.intercept('DELETE', '/api/rebates/*').as('deleteEntityRequest');
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

  });
   */

  afterEach(() => {
    if (rebate) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/rebates/${rebate.id}`,
      }).then(() => {
        rebate = undefined;
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

  it('Rebates menu should load Rebates page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('rebate');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Rebate').should('exist');
    cy.location('pathname').should('eq', rebatePageUrl);
  });

  describe('Rebate page', () => {
    it('should have translated page title', () => {
      cy.visit(rebatePageUrl);
      cy.getEntityHeading('Rebate').should('not.contain', 'gatewayApp.rebate.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(rebatePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Rebate page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${rebatePageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Rebate');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', rebatePageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/rebates',
          body: {
            ...rebateSample,
            account: brokerAccount,
          },
        }).then(({ body }) => {
          rebate = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/rebates+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/rebates?page=0&size=20>; rel="last",<http://localhost/api/rebates?page=0&size=20>; rel="first"',
              },
              body: [rebate],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(rebatePageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(rebatePageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Rebate page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('rebate');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', rebatePageUrl);
      });

      it('edit button click should load edit Rebate page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Rebate');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', rebatePageUrl);
      });

      it('edit button click should load edit Rebate page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Rebate');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', rebatePageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Rebate', () => {
        cy.intercept('GET', '/api/rebates/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('rebate').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', rebatePageUrl);

        rebate = undefined;
      });
    });
  });

  describe('new Rebate page', () => {
    beforeEach(() => {
      cy.visit(rebatePageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Rebate');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Rebate', () => {
      cy.get(`[data-cy="rebateDate"]`).type('2023-12-22');
      cy.get(`[data-cy="rebateDate"]`).blur();
      cy.get(`[data-cy="rebateDate"]`).should('have.value', '2023-12-22');

      cy.get(`[data-cy="amount"]`).type('13908.79');
      cy.get(`[data-cy="amount"]`).should('have.value', '13908.79');

      cy.get(`[data-cy="currency"]`).type('inasmuch equally');
      cy.get(`[data-cy="currency"]`).should('have.value', 'inasmuch equally');

      cy.get(`[data-cy="account"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        rebate = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', rebatePageUrl);
    });
  });
});
