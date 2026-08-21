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

describe('Valuation e2e test', () => {
  const valuationPageUrl = '/valuation';
  let username: string;
  let password: string;
  const valuationSample = { snapshotDate: '2023-12-22', totalValue: 12627.62, currency: 'colorful yieldingly gloomy' };

  let valuation;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/valuations+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/valuations').as('postEntityRequest');
    cy.intercept('DELETE', '/api/valuations/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (valuation) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/valuations/${valuation.id}`,
      }).then(() => {
        valuation = undefined;
      });
    }
  });

  it('Valuations menu should load Valuations page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('valuation');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Valuation').should('exist');
    cy.location('pathname').should('eq', valuationPageUrl);
  });

  describe('Valuation page', () => {
    it('should have translated page title', () => {
      cy.visit(valuationPageUrl);
      cy.getEntityHeading('Valuation').should('not.contain', 'gatewayApp.valuation.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(valuationPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Valuation page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${valuationPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Valuation');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', valuationPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/valuations',
          body: valuationSample,
        }).then(({ body }) => {
          valuation = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/valuations+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/valuations?page=0&size=20>; rel="last",<http://localhost/api/valuations?page=0&size=20>; rel="first"',
              },
              body: [valuation],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(valuationPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Valuation page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('valuation');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', valuationPageUrl);
      });

      it('edit button click should load edit Valuation page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Valuation');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', valuationPageUrl);
      });

      it('edit button click should load edit Valuation page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Valuation');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', valuationPageUrl);
      });

      it('last delete button click should delete instance of Valuation', () => {
        cy.intercept('GET', '/api/valuations/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('valuation').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', valuationPageUrl);

        valuation = undefined;
      });
    });
  });

  describe('new Valuation page', () => {
    beforeEach(() => {
      cy.visit(valuationPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Valuation');
    });

    it('should create an instance of Valuation', () => {
      cy.get(`[data-cy="snapshotDate"]`).type('2023-12-22');
      cy.get(`[data-cy="snapshotDate"]`).blur();
      cy.get(`[data-cy="snapshotDate"]`).should('have.value', '2023-12-22');

      cy.get(`[data-cy="totalValue"]`).type('22589.09');
      cy.get(`[data-cy="totalValue"]`).should('have.value', '22589.09');

      cy.get(`[data-cy="currency"]`).type('glorious nudge bump');
      cy.get(`[data-cy="currency"]`).should('have.value', 'glorious nudge bump');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        valuation = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', valuationPageUrl);
    });
  });
});
