import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { equipmentService } from '../services/equipmentService'
import { loanService } from '../services/loanService'
import { userService } from '../services/userService'
import type {
  Equipment,
  EquipmentStatus,
  Loan,
  LoanStatus,
  User,
  UserRole,
} from '../shared/types/domain'
import './styles/app.css'

const EQUIPMENT_STATUSES: EquipmentStatus[] = [
  'AVAILABLE',
  'RESERVED',
  'UNDER_MAINTENANCE',
]

const LOAN_STATUSES: LoanStatus[] = ['ACTIVE', 'COMPLETED', 'CANCELLED']
const USER_ROLES: UserRole[] = ['ADMIN', 'USER']

const formatDateTime = (value: string): string => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString()
}

function App() {
  const [equipment, setEquipment] = useState<Equipment[]>([])
  const [loans, setLoans] = useState<Loan[]>([])
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [busyAction, setBusyAction] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  const [equipmentName, setEquipmentName] = useState('')
  const [equipmentType, setEquipmentType] = useState('')
  const [equipmentStatus, setEquipmentStatus] = useState<EquipmentStatus>('AVAILABLE')

  const [loanUserId, setLoanUserId] = useState('1')
  const [loanEquipmentId, setLoanEquipmentId] = useState('')

  const [loanStatusDrafts, setLoanStatusDrafts] = useState<Record<number, LoanStatus>>({})
  const [userDrafts, setUserDrafts] = useState<Record<number, { name: string; email: string; role: UserRole }>>({})

  const [userName, setUserName] = useState('')
  const [userEmail, setUserEmail] = useState('')
  const [userRole, setUserRole] = useState<UserRole>('USER')

  const equipmentById = useMemo(() => {
    return new Map(equipment.map((item) => [item.id, item]))
  }, [equipment])

  const setError = (message: string) => {
    setErrorMessage(message)
    setSuccessMessage(null)
  }

  const setSuccess = (message: string) => {
    setSuccessMessage(message)
    setErrorMessage(null)
  }

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const [userResponse, equipmentResponse, loanResponse] = await Promise.all([
        userService.getAll(),
        equipmentService.getAll(),
        loanService.getAll(),
      ])
      setUsers(userResponse)
      setEquipment(equipmentResponse)
      setLoans(loanResponse)
      setUserDrafts(
        Object.fromEntries(
          userResponse.map((user) => [
            user.id,
            {
              name: user.name,
              email: user.email,
              role: user.role,
            },
          ]),
        ) as Record<number, { name: string; email: string; role: UserRole }>,
      )
      setLoanStatusDrafts(
        Object.fromEntries(loanResponse.map((loan) => [loan.id, loan.status])) as Record<
          number,
          LoanStatus
        >,
      )
      setErrorMessage(null)
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to load data.'
      setErrorMessage(message)
      setSuccessMessage(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadData()
  }, [loadData])

  const submitEquipment = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!equipmentName.trim() || !equipmentType.trim()) {
      setError('Equipment name and type are required.')
      return
    }

    setBusyAction('create-equipment')
    try {
      await equipmentService.create({
        name: equipmentName.trim(),
        type: equipmentType.trim(),
        status: equipmentStatus,
      })

      setEquipmentName('')
      setEquipmentType('')
      setEquipmentStatus('AVAILABLE')
      setSuccess('Equipment created successfully.')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to create equipment.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  const submitLoan = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const parsedUserId = Number(loanUserId)
    const parsedEquipmentId = Number(loanEquipmentId)

    if (Number.isNaN(parsedUserId) || parsedUserId <= 0) {
      setError('User ID must be a valid positive number.')
      return
    }

    if (Number.isNaN(parsedEquipmentId) || parsedEquipmentId <= 0) {
      setError('Equipment ID must be a valid positive number.')
      return
    }

    setBusyAction('create-loan')
    try {
      await loanService.create({
        userId: parsedUserId,
        equipmentId: parsedEquipmentId,
      })

      setLoanEquipmentId('')
      setSuccess('Loan created successfully.')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to create loan.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  const submitUser = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!userName.trim()) {
      setError('User name is required.')
      return
    }

    if (!userEmail.trim()) {
      setError('User email is required.')
      return
    }

    setBusyAction('create-user')
    try {
      await userService.create({
        name: userName.trim(),
        email: userEmail.trim(),
        role: userRole,
      })

      setUserName('')
      setUserEmail('')
      setUserRole('USER')
      setSuccess('User created successfully.')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to create user.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  const updateUser = async (user: User) => {
    const draft = userDrafts[user.id] ?? {
      name: user.name,
      email: user.email,
      role: user.role,
    }

    if (!draft.name.trim() || !draft.email.trim()) {
      setError('User name and email are required.')
      return
    }

    setBusyAction(`update-user-${user.id}`)
    try {
      await userService.update(user.id, {
        name: draft.name.trim(),
        email: draft.email.trim(),
        role: draft.role,
      })
      setSuccess(`User ${user.id} updated.`)
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to update user.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  const deleteUser = async (id: number) => {
    setBusyAction(`delete-user-${id}`)
    try {
      await userService.remove(id)
      setSuccess(`User ${id} deleted.`)
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to delete user.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  const reserveEquipment = async (item: Equipment) => {
    setBusyAction(`reserve-equipment-${item.id}`)
    try {
      await equipmentService.reserve(item.id)
      setSuccess(`Equipment ${item.id} reserved.`)
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to reserve equipment.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  const changeEquipmentStatus = async (item: Equipment, nextStatus: EquipmentStatus) => {
    setBusyAction(`status-equipment-${item.id}`)
    try {
      await equipmentService.update(item.id, {
        name: item.name,
        type: item.type,
        status: nextStatus,
      })
      setSuccess(`Equipment ${item.id} updated.`)
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to update equipment status.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  const deleteEquipment = async (id: number) => {
    setBusyAction(`delete-equipment-${id}`)
    try {
      await equipmentService.remove(id)
      setSuccess(`Equipment ${id} deleted.`)
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to delete equipment.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  const updateLoanStatus = async (loan: Loan) => {
    const nextStatus = loanStatusDrafts[loan.id] ?? loan.status
    setBusyAction(`update-loan-${loan.id}`)
    try {
      await loanService.update(loan.id, {
        userId: loan.userId,
        equipmentId: loan.equipmentId,
        status: nextStatus,
      })
      setSuccess(`Loan ${loan.id} updated.`)
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to update loan status.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  const deleteLoan = async (id: number) => {
    setBusyAction(`delete-loan-${id}`)
    try {
      await loanService.remove(id)
      setSuccess(`Loan ${id} deleted.`)
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to delete loan.'
      setError(message)
    } finally {
      setBusyAction(null)
    }
  }

  return (
    <main className="app-shell">
      <header className="app-header">
        <div>
          <h1>LabLend Dashboard</h1>
        </div>
        <button
          type="button"
          className="secondary-button"
          disabled={loading || busyAction !== null}
          onClick={() => {
            void loadData()
          }}
        >
          Refresh
        </button>
      </header>

      {errorMessage && (
        <p className="status-banner error" role="alert">
          {errorMessage}
        </p>
      )}
      {successMessage && <p className="status-banner success">{successMessage}</p>}

      <section className="panel-grid">
        <section className="panel">
          <h2>Create Equipment</h2>
          <form className="stack" onSubmit={submitEquipment}>
        <label>
          Name
          <input
            value={equipmentName}
            onChange={(event) => setEquipmentName(event.target.value)}
            placeholder="Microscope"
          />
        </label>
        <label>
          Type
          <input
            value={equipmentType}
            onChange={(event) => setEquipmentType(event.target.value)}
            placeholder="Optics"
          />
        </label>
        <label>
          Status
          <select
            value={equipmentStatus}
            onChange={(event) => setEquipmentStatus(event.target.value as EquipmentStatus)}
          >
            {EQUIPMENT_STATUSES.map((status) => (
          <option value={status} key={status}>
            {status}
          </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={busyAction === 'create-equipment'}>
          {busyAction === 'create-equipment' ? 'Creating...' : 'Create equipment'}
        </button>
          </form>
        </section>

        <section className="panel">
          <h2>Create User</h2>
          <form className="stack" onSubmit={submitUser}>
        <label>
          Name
          <input
            value={userName}
            onChange={(event) => setUserName(event.target.value)}
            placeholder="Ada Lovelace"
          />
        </label>
        <label>
          Email
          <input
            value={userEmail}
            onChange={(event) => setUserEmail(event.target.value)}
            placeholder="ada@lablend.dev"
          />
        </label>
        <label>
          Role
          <select value={userRole} onChange={(event) => setUserRole(event.target.value as UserRole)}>
            {USER_ROLES.map((role) => (
          <option value={role} key={role}>
            {role}
          </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={busyAction === 'create-user'}>
          {busyAction === 'create-user' ? 'Creating...' : 'Create user'}
        </button>
          </form>
        </section>

        <section className="panel">
          <h2>Create Loan</h2>
          <form className="stack" onSubmit={submitLoan}>
        <label>
          User ID
          <select value={loanUserId} onChange={(event) => setLoanUserId(event.target.value)}>
            {users.length === 0 ? (
          <option value="">No users available</option>
            ) : (
          users.map((user) => (
            <option value={String(user.id)} key={user.id}>
              {user.id} - {user.name}
            </option>
          ))
            )}
          </select>
        </label>
        <label>
          Equipment ID
          <select
            value={loanEquipmentId}
            onChange={(event) => setLoanEquipmentId(event.target.value)}
          >
            {equipment.length === 0 ? (
          <option value="">No equipment available</option>
            ) : (
          equipment.map((item) => (
            <option value={String(item.id)} key={item.id}>
              {item.id} - {item.name}
            </option>
          ))
            )}
          </select>
        </label>
        <button type="submit" disabled={busyAction === 'create-loan'}>
          {busyAction === 'create-loan' ? 'Creating...' : 'Create loan'}
        </button>
          </form>
        </section>
      </section>

      <section className="panel">
        <h2>Users</h2>
        {loading ? (
          <p className="muted">Loading users...</p>
        ) : users.length === 0 ? (
          <p className="muted">No users found.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => {
                  const draft = userDrafts[user.id] ?? {
                    name: user.name,
                    email: user.email,
                    role: user.role,
                  }

                  return (
                    <tr key={user.id}>
                      <td>{user.id}</td>
                      <td>
                        <input
                          value={draft.name}
                          onChange={(event) => {
                            setUserDrafts((previous) => ({
                              ...previous,
                              [user.id]: {
                                ...draft,
                                name: event.target.value,
                              },
                            }))
                          }}
                        />
                      </td>
                      <td>
                        <input
                          value={draft.email}
                          onChange={(event) => {
                            setUserDrafts((previous) => ({
                              ...previous,
                              [user.id]: {
                                ...draft,
                                email: event.target.value,
                              },
                            }))
                          }}
                        />
                      </td>
                      <td>
                        <select
                          value={draft.role}
                          onChange={(event) => {
                            setUserDrafts((previous) => ({
                              ...previous,
                              [user.id]: {
                                ...draft,
                                role: event.target.value as UserRole,
                              },
                            }))
                          }}
                        >
                          {USER_ROLES.map((role) => (
                            <option value={role} key={role}>
                              {role}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            disabled={busyAction === `update-user-${user.id}`}
                            onClick={() => {
                              void updateUser(user)
                            }}
                          >
                            {busyAction === `update-user-${user.id}` ? 'Saving...' : 'Save'}
                          </button>
                          <button
                            type="button"
                            className="danger"
                            disabled={busyAction === `delete-user-${user.id}`}
                            onClick={() => {
                              void deleteUser(user.id)
                            }}
                          >
                            {busyAction === `delete-user-${user.id}` ? 'Deleting...' : 'Delete'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel">
        <h2>Equipment</h2>
        {loading ? (
          <p className="muted">Loading equipment...</p>
        ) : equipment.length === 0 ? (
          <p className="muted">No equipment found.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Type</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {equipment.map((item) => (
                  <tr key={item.id}>
                    <td>{item.id}</td>
                    <td>{item.name}</td>
                    <td>{item.type}</td>
                    <td>{item.status}</td>
                    <td>
                      <div className="row-actions">
                        <button
                          type="button"
                          disabled={
                            item.status !== 'AVAILABLE' ||
                            busyAction === `reserve-equipment-${item.id}`
                          }
                          onClick={() => {
                            void reserveEquipment(item)
                          }}
                        >
                          {busyAction === `reserve-equipment-${item.id}` ? 'Reserving...' : 'Reserve'}
                        </button>
                        <select
                          defaultValue={item.status}
                          onChange={(event) => {
                            void changeEquipmentStatus(item, event.target.value as EquipmentStatus)
                          }}
                        >
                          {EQUIPMENT_STATUSES.map((status) => (
                            <option value={status} key={status}>
                              {status}
                            </option>
                          ))}
                        </select>
                        <button
                          type="button"
                          className="danger"
                          disabled={busyAction === `delete-equipment-${item.id}`}
                          onClick={() => {
                            void deleteEquipment(item.id)
                          }}
                        >
                          {busyAction === `delete-equipment-${item.id}` ? 'Deleting...' : 'Delete'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel">
        <h2>Loans</h2>
        {loading ? (
          <p className="muted">Loading loans...</p>
        ) : loans.length === 0 ? (
          <p className="muted">No loans found.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User</th>
                  <th>Equipment</th>
                  <th>Equipment Name</th>
                  <th>Date</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loans.map((loan) => {
                  const relatedEquipment = equipmentById.get(loan.equipmentId)
                  const selectedStatus = loanStatusDrafts[loan.id] ?? loan.status
                  return (
                    <tr key={loan.id}>
                      <td>{loan.id}</td>
                      <td>{loan.userId}</td>
                      <td>{loan.equipmentId}</td>
                      <td>{relatedEquipment ? relatedEquipment.name : '-'}</td>
                      <td>{formatDateTime(loan.loanDate)}</td>
                      <td>{loan.status}</td>
                      <td>
                        <div className="row-actions">
                          <select
                            value={selectedStatus}
                            onChange={(event) => {
                              setLoanStatusDrafts((previous) => ({
                                ...previous,
                                [loan.id]: event.target.value as LoanStatus,
                              }))
                            }}
                          >
                            {LOAN_STATUSES.map((status) => (
                              <option value={status} key={status}>
                                {status}
                              </option>
                            ))}
                          </select>
                          <button
                            type="button"
                            disabled={busyAction === `update-loan-${loan.id}`}
                            onClick={() => {
                              void updateLoanStatus(loan)
                            }}
                          >
                            {busyAction === `update-loan-${loan.id}` ? 'Saving...' : 'Save'}
                          </button>
                          <button
                            type="button"
                            className="danger"
                            disabled={busyAction === `delete-loan-${loan.id}`}
                            onClick={() => {
                              void deleteLoan(loan.id)
                            }}
                          >
                            {busyAction === `delete-loan-${loan.id}` ? 'Deleting...' : 'Delete'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  )
}

export default App
