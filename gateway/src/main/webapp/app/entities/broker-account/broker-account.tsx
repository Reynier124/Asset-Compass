import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { Translate, getSortState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities } from './broker-account.reducer';

export const BrokerAccount = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const brokerAccountList = useAppSelector(state => state.gateway.brokerAccount.entities);
  const loading = useAppSelector(state => state.gateway.brokerAccount.loading);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        sort: `${sortState.sort},${sortState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?sort=${sortState.sort},${sortState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [sortState.order, sortState.sort]);

  const sort = p => () => {
    setSortState({
      ...sortState,
      order: sortState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = sortState.sort;
    const { order } = sortState;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="broker-account-heading" data-cy="BrokerAccountHeading">
        <Translate contentKey="gatewayApp.brokerAccount.home.title">Broker Accounts</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="gatewayApp.brokerAccount.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/broker-account/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="gatewayApp.brokerAccount.home.createLabel">Create new Broker Account</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {brokerAccountList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="gatewayApp.brokerAccount.id">Id</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('externalAccountId')}>
                  <Translate contentKey="gatewayApp.brokerAccount.externalAccountId">External Account Id</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('externalAccountId')} />
                </th>
                <th className="hand" onClick={sort('displayName')}>
                  <Translate contentKey="gatewayApp.brokerAccount.displayName">Display Name</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('displayName')} />
                </th>
                <th>
                  <Translate contentKey="gatewayApp.brokerAccount.broker">Broker</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {brokerAccountList.map(brokerAccount => (
                <tr key={`entity-${brokerAccount.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/broker-account/${brokerAccount.id}`} variant="link" size="sm">
                      {brokerAccount.id}
                    </Button>
                  </td>
                  <td>{brokerAccount.externalAccountId}</td>
                  <td>{brokerAccount.displayName}</td>
                  <td>{brokerAccount.broker ? <Link to={`/broker/${brokerAccount.broker.id}`}>{brokerAccount.broker.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/broker-account/${brokerAccount.id}`}
                        variant="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/broker-account/${brokerAccount.id}/edit`}
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
                        onClick={() => (globalThis.location.href = `/broker-account/${brokerAccount.id}/delete`)}
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
              <Translate contentKey="gatewayApp.brokerAccount.home.notFound">No Broker Accounts found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default BrokerAccount;
