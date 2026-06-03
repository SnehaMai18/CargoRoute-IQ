import React, { useState, useEffect, useCallback, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout';
import { AuthContext } from '../../auth/AuthContext';
import {
  getAcknowledgementById,
  updateAcknowledgement,
} from '../../api/dispatchApi';
import { DISPATCH_STATUS_CONFIG, DRIVER_STATUS_CONFIG, VEHICLE_TYPE_CONFIG } from '../../utils/constants';
import '../../styles/Bookings.css';
import '../../styles/DispatchManifests.css';
import '../../styles/AcknowledgementDetail.css';
 
// ── Formatters ────────────────────────────────────────────────────────────────
 
function formatAckId(id) {
  return `ACK${String(id).padStart(4, '0')}`;
}
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
function formatDate(dt) {
  if (!dt) return '–';
  return new Date(dt).toLocaleDateString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric',
  });
}
 
// ── Component ─────────────────────────────────────────────────────────────────
 
export default function AcknowledgementDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const isDriver = user?.role?.toLowerCase() === 'driver';
 
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editNotes, setEditNotes] = useState(false);
  const [notes, setNotes] = useState('');
  const [updating, setUpdating] = useState(false);
  const [updateMsg, setUpdateMsg] = useState('');
  const [updateError, setUpdateError] = useState('');
 
  // ── Loaders ──────────────────────────────────────────────────────────────
 
  const loadAcknowledgement = useCallback(() => {
    setLoading(true);
    setError('');
    getAcknowledgementById(id)
      .then((data) => {
        setItem(data);
        setNotes(data?.notes || '');
      })
      .catch(() => setError('Acknowledgement not found or service unavailable.'))
      .finally(() => setLoading(false));
  }, [id]);
 
  useEffect(() => {
    loadAcknowledgement();
  }, [loadAcknowledgement]);
 
  // ── Update Notes ─────────────────────────────────────────────────────────
 
  const handleUpdateNotes = () => {
    setUpdateMsg('');
    setUpdateError('');
    setUpdating(true);
 
    const payload = {
      dispatchID: item?.dispatch?.dispatch?.dispatchID,
      driverID: item?.driver?.driverID,
      notes: notes.trim() || null,
    };
 
    updateAcknowledgement(id, payload)
      .then(() => {
        setUpdateMsg('Notes updated successfully.');
        setEditNotes(false);
        loadAcknowledgement();
      })
      .catch(() => setUpdateError('Update failed. Please try again.'))
      .finally(() => setUpdating(false));
  };
 
  // ── Loading / error ───────────────────────────────────────────────────────
 
  if (loading) {
    return <Layout><div className="loading-spinner">Loading acknowledgement…</div></Layout>;
  }
 
  if (error || !item) {
    return (
      <Layout>
        <div className="auth-message auth-message-error">
          ⚠ {error || 'Acknowledgement not found.'}
          <button className="btn-secondary ack-detail-back-btn-margin" onClick={() => navigate('/acknowledgements')}>
            ← Back
          </button>
        </div>
      </Layout>
    );
  }
 
  const ack = item || {};
  const dispatch = ack.dispatch?.dispatch || {};
  const load = ack.dispatch?.load || {};
  const vehicle = ack.dispatch?.vehicle || {};
  const driver = ack.driver || {};
  const st = DISPATCH_STATUS_CONFIG[dispatch.status] || { label: dispatch.status, cls: '' };
  const drSt = DRIVER_STATUS_CONFIG[driver.status] || { label: driver.status, cls: '' };
  const vtCfg = VEHICLE_TYPE_CONFIG[vehicle.type] || { label: vehicle.type || '–' };
 
  // ── Render ────────────────────────────────────────────────────────────────
 
  return (
    <Layout>
      <div className="bookings-page booking-detail-page dispatch-page">
 
        {/* ── Header ── */}
        <div className="detail-header">
          <div className="detail-header-left">
            <button className="back-btn" onClick={() => navigate('/acknowledgements')} title="Back">
              ←
            </button>
            <span className="detail-booking-id">{formatAckId(ack.ackID)}</span>
          </div>
        </div>
 
        {/* ── Detail Grid ── */}
        <div className="detail-grid">
 
          {/* Acknowledgement Info */}
          <div className="detail-card">
            <p className="detail-card-title">Acknowledgement Details</p>
            <div className="detail-row-2">
              <div className="detail-field">
                <span className="detail-label">Acknowledgement ID</span>
                <span className="detail-value">{formatAckId(ack.ackID)}</span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Acknowledged At</span>
                <span className="detail-value">{formatDateTime(ack.ackAt)}</span>
              </div>
            </div>
          </div>
 
          {/* Driver Info */}
          <div className="detail-card">
            <p className="detail-card-title">Driver Information</p>
            <div className="detail-row-2">
              <div className="detail-field">
                <span className="detail-label">Driver Name</span>
                <span className="detail-value">{driver.name || '–'}</span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Driver ID</span>
                <span className="detail-value">{driver.driverID || '–'}</span>
              </div>
              <div className="detail-field">
                <span className="detail-label">License No</span>
                <span className="detail-value">{driver.licenseNo || '–'}</span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Mobile Number</span>
                <span className="detail-value">{driver.mobileNumber || driver.phoneNumber || '–'}</span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Status</span>
                <span className="detail-value">
                  <span className={`status-badge ${drSt.cls}`}>{drSt.label}</span>
                </span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Contact Info</span>
                <span className="detail-value">{driver.contactInfo || driver.email || '–'}</span>
              </div>
            </div>
          </div>
 
          {/* Dispatch Info */}
          <div className="detail-card">
            <p className="detail-card-title">Dispatch Information</p>
            <div className="detail-row-2">
              <div className="detail-field">
                <span className="detail-label">Dispatch ID</span>
                <span className="detail-value">{formatDispatchId(dispatch.dispatchID)}</span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Status</span>
                <span className="detail-value">
                  <span className={`status-badge ${st.cls}`}>{st.label}</span>
                </span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Assigned By</span>
                <span className="detail-value">{dispatch.assignedBy || '–'}</span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Assigned At</span>
                <span className="detail-value">{formatDateTime(dispatch.assignedAt || dispatch.createdAt)}</span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Assigned Driver ID</span>
                <span className="detail-value">{dispatch.assignedDriverID || '–'}</span>
              </div>
              <div className="detail-field">
                <span className="detail-label">Load ID</span>
                <span className="detail-value">{formatLoadId(dispatch.loadID)}</span>
              </div>
            </div>
          </div>
 
          {/* Load Info */}
          {load.loadID && (
            <div className="detail-card">
              <p className="detail-card-title">Load Information</p>
              <div className="detail-row-2">
                <div className="detail-field">
                  <span className="detail-label">Load Code</span>
                  <span className="detail-value">{load.loadCode || '–'}</span>
                </div>
                <div className="detail-field">
                  <span className="detail-label">Status</span>
                  <span className="detail-value">{load.status || '–'}</span>
                </div>
                <div className="detail-field">
                  <span className="detail-label">Weight / Volume</span>
                  <span className="detail-value">
                    {load.totalWeightKg != null ? `${load.totalWeightKg} kg` : '–'}
                    {load.totalVolumeM3 != null ? ` · ${load.totalVolumeM3} m³` : ''}
                  </span>
                </div>
                <div className="detail-field">
                  <span className="detail-label">Planned Start</span>
                  <span className="detail-value">{formatDateTime(load.plannedStart || load.pickupDate)}</span>
                </div>
                <div className="detail-field">
                  <span className="detail-label">Planned End</span>
                  <span className="detail-value">{formatDateTime(load.plannedEnd || load.deliveryDate)}</span>
                </div>
              </div>
            </div>
          )}
 
          {/* Vehicle Info */}
          {vehicle.vehicleID && (
            <div className="detail-card">
              <p className="detail-card-title">Vehicle Information</p>
              <div className="detail-row-2">
                <div className="detail-field">
                  <span className="detail-label">Reg Number</span>
                  <span className="detail-value">{vehicle.regNumber || '–'}</span>
                </div>
                <div className="detail-field">
                  <span className="detail-label">Type</span>
                  <span className="detail-value">{vtCfg.label}</span>
                </div>
                <div className="detail-field">
                  <span className="detail-label">Max Capacity</span>
                  <span className="detail-value">
                    {vehicle.maxWeightKg != null ? `${vehicle.maxWeightKg} kg` : '–'}
                    {vehicle.maxVolumeM3 != null ? ` · ${vehicle.maxVolumeM3} m³` : ''}
                  </span>
                </div>
                <div className="detail-field">
                  <span className="detail-label">Vehicle Status</span>
                  <span className="detail-value">{vehicle.status || '–'}</span>
                </div>
                <div className="detail-field">
                  <span className="detail-label">Last Maintenance</span>
                  <span className="detail-value">{formatDate(vehicle.lastMaintenanceAt)}</span>
                </div>
              </div>
            </div>
          )}
 
          {/* Notes */}
          <div className="detail-card">
            <p className="detail-card-title">Notes</p>
            {editNotes ? (
              <div>
                <textarea
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  maxLength={300}
                  className="ack-notes-textarea"
                  placeholder="Add notes about this acknowledgement…"
                />
                <div className="ack-char-counter">{notes.length}/300 characters</div>
                <div className="ack-button-group">
                  <button
                    type="button"
                    className="btn-primary"
                    onClick={handleUpdateNotes}
                    disabled={updating}
                  >
                    {updating ? 'Saving…' : 'Save'}
                  </button>
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={() => { setEditNotes(false); setNotes(item?.notes || ''); }}
                    disabled={updating}
                  >
                    Cancel
                  </button>
                </div>
              </div>
            ) : (
              <div>
                <p className={`ack-notes-text ${!ack.notes ? 'empty' : ''}`}>
                  {ack.notes || 'No notes added.'}
                </p>
                {isDriver && (
                  <button
                    type="button"
                    className="btn-secondary ack-edit-notes-btn"
                    onClick={() => setEditNotes(true)}
                  >
                    Edit Notes
                  </button>
                )}
              </div>
            )}
            {updateMsg && (
              <div className="auth-message auth-message-success ack-message-margin">
                ✔ {updateMsg}
              </div>
            )}
            {updateError && (
              <div className="auth-message auth-message-error ack-message-margin">
                ⚠ {updateError}
              </div>
            )}
          </div>
 
        </div>
 
      </div>
    </Layout>
  );
}
 
 