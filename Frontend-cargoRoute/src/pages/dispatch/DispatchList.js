import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout';
import DashboardTemplate from '../../components/DashboardTemplate';
import Pagination1 from '../../components/Pagination1';
import ActionMenuPortal from '../../components/ActionMenuPortal';
import { getAllDispatches, deleteDispatch } from '../../api/dispatchApi';
import { DISPATCH_STATUS_CONFIG } from '../../utils/constants';
import { exportCSV } from '../../utils/csvExport';

import '../../styles/Bookings.css';
import '../../styles/DispatchManifests.css';
import '../../styles/Fleet.css';

// ── Formatters ───────────────────────────────────────────────────────────────

function formatDispatchId(id) {
  return `DS${String(id).padStart(4, '0')}`;
}

function formatLoadId(id) {
  return id ? `LD${String(id).padStart(4, '0')}` : '–';
}

function formatDateTime(dt) {
  if (!dt) return '–';
  return new Date(dt).toLocaleString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

function getDispatchId(item) {
  const id = item?.dispatch?.dispatchID ?? item?.dispatchID ?? item?.id ?? item?.dispatch?.id;
  return Number(id) || 0;
}

// ── Component ────────────────────────────────────────────────────────────────

const PAGE_SIZE = 4;

export default function DispatchList() {
  const navigate = useNavigate();

  const [dispatches, setDispatches] = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState('');
  const [search, setSearch]         = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [currentPage, setCurrentPage]   = useState(1);

  const loadDispatches = useCallback(() => {
    setLoading(true);
    setError('');
    getAllDispatches()
      .then(setDispatches)
      .catch(() => setError('Could not load dispatches. Is DispatchService running?'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { loadDispatches(); }, [loadDispatches]);

  // ── Filtering ─────────────────────────────────────────────────────────────

  const filtered = dispatches.filter((item) => {
    const d = item.dispatch || {};
    const l = item.load     || {};
    const v = item.vehicle  || {};
    const q = search.toLowerCase().trim();

    const matchSearch =
      !q ||
      formatDispatchId(d.dispatchID).toLowerCase().includes(q) ||
      formatLoadId(d.loadID).toLowerCase().includes(q) ||
      (l.loadCode || '').toLowerCase().includes(q) ||
      (v.regNumber || '').toLowerCase().includes(q) ||
      (d.assignedBy || '').toLowerCase().includes(q);

    const matchStatus = statusFilter === 'ALL' || d.status === statusFilter;
    return matchSearch && matchStatus;
  });

  // Reset to page 1 when filters or data change
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { setCurrentPage(1); }, [search, statusFilter, dispatches]);

  const sortedDispatches = [...filtered].sort((a, b) => getDispatchId(b) - getDispatchId(a));
  const totalPages = Math.max(1, Math.ceil(sortedDispatches.length / PAGE_SIZE));
  const paginated  = sortedDispatches.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

  // ── Stats ─────────────────────────────────────────────────────────────────

  const stats = {
    total:      dispatches.length,
    pending:    dispatches.filter((i) => ['CREATED','PENDING','ASSIGNED'].includes(i.dispatch?.status)).length,
    inProgress: dispatches.filter((i) => ['ACKNOWLEDGED','IN_PROGRESS'].includes(i.dispatch?.status)).length,
    completed:  dispatches.filter((i) => i.dispatch?.status === 'COMPLETED').length,
  };

  const handleExport = () => {
    const headers = ['Dispatch ID', 'Load ID', 'Vehicle', 'Driver', 'Assigned By', 'Assigned At', 'Status'];
    const rows = sortedDispatches.map((item) => {
      const d = item.dispatch || {};
      const l = item.load || {};
      const v = item.vehicle || {};
      return [
        formatDispatchId(d.dispatchID),
        formatLoadId(l.loadID),
        v.regNumber || '–',
        v.driver?.name || '–',
        d.assignedBy || '–',
        formatDateTime(d.assignedAt),
        d.status || '–',
      ];
    });
    exportCSV('dispatches.csv', headers, rows);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this dispatch? This cannot be undone.')) return;
    try {
      await deleteDispatch(id);
      loadDispatches();
    } catch {
      setError('Delete failed.');
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <Layout>
      <DashboardTemplate
        title="Dispatch"
        subtitle="Assign loads to drivers and track dispatch status"
        actionButtonLabel="+"
        onActionButtonClick={() => navigate('/dispatch/new')}
        kpiCards={[
          { label: 'TOTAL DISPATCHES', value: stats.total },
          { label: 'PENDING / ASSIGNED', value: stats.pending },
          { label: 'IN PROGRESS', value: stats.inProgress },
          { label: 'COMPLETED', value: stats.completed },
        ]}
        contentCardTitle="All Dispatches"
        searchPlaceholder="Search by dispatch ID, load code, vehicle, or assigned by…"
        searchValue={search}
        onSearchChange={(e) => setSearch(e.target.value)}
        filters={[
          {
            label: 'Status',
            value: statusFilter,
            options: [
              { label: 'Status', value: 'ALL' },
              ...Object.entries(DISPATCH_STATUS_CONFIG).map(([k, v]) => ({ label: v.label, value: k })),
            ],
            onChange: (e) => setStatusFilter(e.target.value),
          },
        ]}
        exportButtonLabel="Export"
        onExport={handleExport}
        loading={loading}
        error={error}
      >
        <div className="table-wrapper">
          <table className="bookings-table">
            <thead>
              <tr>
                <th>Dispatch ID</th>
                <th>Load</th>
                <th>Vehicle</th>
                <th>Driver</th>
                <th>Assigned By</th>
                <th>Assigned At</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {paginated.length === 0 ? (
                <tr>
                  <td colSpan={8} className="empty-state">
                    {dispatches.length === 0
                      ? 'No dispatches yet. Create one to get started.'
                      : 'No dispatches match your search.'}
                  </td>
                </tr>
              ) : (
                paginated.map((item) => {
                  const d  = item.dispatch || {};
                  const l  = item.load     || {};
                  const v  = item.vehicle  || {};
                  const dr = v.driver      || {};
                  const st = DISPATCH_STATUS_CONFIG[d.status] || { label: d.status, cls: '' };
                  return (
                    <tr
                      key={d.dispatchID}
                      className="table-row"
                      onClick={() => navigate(`/dispatch/${d.dispatchID}`)}
                    >
                      <td className="booking-id-cell">{formatDispatchId(d.dispatchID)}</td>
                      <td>
                        <div className="route-cell">
                          <span className="route-origin booking-id-cell">{formatLoadId(d.loadID)}</span>
                          {l.loadCode && <span className="route-arrow">{l.loadCode}</span>}
                        </div>
                      </td>
                      <td>
                        {v.regNumber ? (
                          <div className="route-cell">
                            <span className="route-origin">{v.regNumber}</span>
                            <span className="route-arrow">{v.type || ''}</span>
                          </div>
                        ) : '–'}
                      </td>
                      <td>{dr.name || '–'}</td>
                      <td style={{ textAlign: 'left' }}>{d.assignedBy || '–'}</td>
                      <td style={{ textAlign: 'left' }}>{formatDateTime(d.assignedAt)}</td>
                      <td>
                        <span className={`status-badge ${st.cls}`}>{st.label}</span>
                      </td>
                      <td>
                        <ActionMenuPortal
                          id={d.dispatchID}
                          actions={[
                            { label: 'View', onClick: () => navigate(`/dispatch/${d.dispatchID}`) },
                            { label: 'Delete', onClick: () => handleDelete(d.dispatchID), danger: true }
                          ]}
                        />
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <Pagination1
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        )}
      </DashboardTemplate>
    </Layout>
  );
}
