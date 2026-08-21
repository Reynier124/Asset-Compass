import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './rebate.reducer';

export const Rebate = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const rebateList = useAppSelector(state => state.gateway.rebate.entities);
  const loading = useAppSelector(state => state.gateway.rebate.loading);
  const totalItems = useAppSelector(state => state.gateway.rebate.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const { order } = paginationState;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="rebate-heading" data-cy="RebateHeading">
        <Translate contentKey="gatewayApp.rebate.home.title">Rebates</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="gatewayApp.rebate.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/rebate/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="gatewayApp.rebate.home.createLabel">Create new Rebate</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {rebateList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="gatewayApp.rebate.id">Id</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('rebateDate')}>
                  <Translate contentKey="gatewayApp.rebate.rebateDate">Rebate Date</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('rebateDate')} />
                </th>
                <th className="hand" onClick={sort('amount')}>
                  <Translate contentKey="gatewayApp.rebate.amount">Amount</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('amount')} />
                </th>
                <th className="hand" onClick={sort('currency')}>
                  <Translate contentKey="gatewayApp.rebate.currency">Currency</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('currency')} />
                </th>
                <th>
                  <Translate contentKey="gatewayApp.rebate.account">Account</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="gatewayApp.rebate.operation">Operation</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rebateList.map(rebate => (
                <tr key={`entity-${rebate.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/rebate/${rebate.id}`} variant="link" size="sm">
                      {rebate.id}
                    </Button>
                  </td>
                  <td>{rebate.rebateDate ? <TextFormat type="date" value={rebate.rebateDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{rebate.amount}</td>
                  <td>{rebate.currency}</td>
                  <td>{rebate.account ? <Link to={`/broker-account/${rebate.account.id}`}>{rebate.account.id}</Link> : ''}</td>
                  <td>{rebate.operation ? <Link to={`/operation/${rebate.operation.id}`}>{rebate.operation.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button as={Link as any} to={`/rebate/${rebate.id}`} variant="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/rebate/${rebate.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() =>
                          (globalThis.location.href = `/rebate/${rebate.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
                        variant="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="gatewayApp.rebate.home.notFound">No Rebates found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={rebateList && rebateList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default Rebate;
