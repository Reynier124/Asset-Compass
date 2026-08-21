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

describe('Position e2e test', () => {
  const positionPageUrl = '/position';
  let username: string;
  let password: string;
  // const positionSample = {"quantity":424.47,"averageCost":28673.47,"currentValue":4634.23,"currency":"because apparatus among","lastSyncedAt":"2023-12-22T06:18:32.329Z"};

  let position;
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
      body: {"externalAccountId":"famously difficult","displayName":"supposing"},
    }).then(({ body }) => {
      brokerAccount = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/assets',
      body: {"ticket":"whenever","category":"lest almost","country":"Bhutan","description":"for but"},
    }).then(({ body }) => {
      asset = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/positions+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/positions').as('postEntityRequest');
    cy.intercept('DELETE', '/api/positions/*').as('deleteEntityRequest');
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
    if (position) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/positions/${position.id}`,
      }).then(() => {
        position = undefined;
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

  it('Positions menu should load Positions page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('position');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Position').should('exist');
    cy.location('pathname').should('eq', positionPageUrl);
  });

  describe('Position page', () => {
    it('should have translated page title', () => {
      cy.visit(positionPageUrl);
      cy.getEntityHeading('Position').should('not.contain', 'gatewayApp.position.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(positionPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Position page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${positionPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Position');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', positionPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/positions',
          body: {
            ...positionSample,
            account: brokerAccount,
            asset: asset,
          },
        }).then(({ body }) => {
          position = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/positions+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/positions?page=0&size=20>; rel="last",<http://localhost/api/positions?page=0&size=20>; rel="first"',
              },
              body: [position],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(positionPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(positionPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Position page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('position');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', positionPageUrl);
      });

      it('edit button click should load edit Position page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Position');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', positionPageUrl);
      });

      it('edit button click should load edit Position page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Position');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', positionPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Position', () => {
        cy.intercept('GET', '/api/positions/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('position').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', positionPageUrl);

        position = undefined;
      });
    });
  });

  describe('new Position page', () => {
    beforeEach(() => {
      cy.visit(positionPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Position');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Position', () => {
      cy.get(`[data-cy="quantity"]`).type('763.6');
      cy.get(`[data-cy="quantity"]`).should('have.value', '763.6');

      cy.get(`[data-cy="averageCost"]`).type('9888.42');
      cy.get(`[data-cy="averageCost"]`).should('have.value', '9888.42');

      cy.get(`[data-cy="currentValue"]`).type('26549.98');
      cy.get(`[data-cy="currentValue"]`).should('have.value', '26549.98');

      cy.get(`[data-cy="currency"]`).type('so indeed bah');
      cy.get(`[data-cy="currency"]`).should('have.value', 'so indeed bah');

      cy.get(`[data-cy="lastSyncedAt"]`).type('2023-12-22T07:19');
      cy.get(`[data-cy="lastSyncedAt"]`).blur();
      cy.get(`[data-cy="lastSyncedAt"]`).should('have.value', '2023-12-22T07:19');

      cy.get(`[data-cy="account"]`).select(1);
      cy.get(`[data-cy="asset"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        position = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', positionPageUrl);
    });
  });
});
