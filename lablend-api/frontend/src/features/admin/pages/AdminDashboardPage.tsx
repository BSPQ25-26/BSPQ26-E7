import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Container,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  Typography,
} from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { equipmentService } from '../../../services/equipmentService'
import { loanService } from '../../../services/loanService'
import { userService } from '../../../services/userService'
import { useAuth } from '../../auth/context/AuthContext'
import type { Equipment, EquipmentStatus, Loan, LoanStatus, User, UserRole } from '../../../shared/types/domain'
import { AppSnackbar } from '../../../shared/ui/AppSnackbar'
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog'
import { SectionHeader } from '../../../shared/ui/SectionHeader'

const EQUIPMENT_STATUSES: EquipmentStatus[] = ['AVAILABLE', 'RESERVED', 'UNDER_MAINTENANCE']
const LOAN_STATUSES: LoanStatus[] = ['ACTIVE', 'COMPLETED', 'CANCELLED']
const USER_ROLES: UserRole[] = ['ADMIN', 'USER']

type AdminSection = 'people' | 'assets' | 'loans'

type SnackbarState = {
  message: string
  severity: 'success' | 'error' | 'info' | 'warning'
}

type ConfirmState = {
  title: string
  description: string
  confirmLabel: string
  danger: boolean
  action: () => Promise<void>
} | null

const formatDateTime = (value: string): string => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString()
}

const SectionTab = (props: { label: string; value: AdminSection }) => <Tab {...props} />

export const AdminDashboardPage = () => {
  const { logout, session } = useAuth()
  const [activeSection, setActiveSection] = useState<AdminSection>('people')

  const [equipment, setEquipment] = useState<Equipment[]>([])
  const [loans, setLoans] = useState<Loan[]>([])
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [busyAction, setBusyAction] = useState<string | null>(null)
  const [snackbar, setSnackbar] = useState<SnackbarState | null>(null)
  const [confirmState, setConfirmState] = useState<ConfirmState>(null)

  const [equipmentName, setEquipmentName] = useState('')
  const [equipmentType, setEquipmentType] = useState('')
  const [equipmentStatus, setEquipmentStatus] = useState<EquipmentStatus>('AVAILABLE')

  const [loanUserId, setLoanUserId] = useState('')
  const [loanEquipmentId, setLoanEquipmentId] = useState('')

  const [loanStatusDrafts, setLoanStatusDrafts] = useState<Record<number, LoanStatus>>({})
  const [userDrafts, setUserDrafts] = useState<Record<number, { name: string; email: string; role: UserRole }>>({})

  const [userName, setUserName] = useState('')
  const [userEmail, setUserEmail] = useState('')
  const [userPassword, setUserPassword] = useState('')
  const [userRole, setUserRole] = useState<UserRole>('USER')

  const equipmentById = useMemo(() => new Map(equipment.map((item) => [item.id, item])), [equipment])
  const stats = useMemo(
    () => ({
      users: users.length,
      equipment: equipment.length,
      available: equipment.filter((item) => item.status === 'AVAILABLE').length,
      loans: loans.length,
    }),
    [equipment, loans, users],
  )

  const notify = (message: string, severity: SnackbarState['severity']) => {
    setSnackbar({ message, severity })
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
            { name: user.name, email: user.email, role: user.role },
          ]),
        ) as Record<number, { name: string; email: string; role: UserRole }>,
      )
      setLoanStatusDrafts(
        Object.fromEntries(loanResponse.map((loan) => [loan.id, loan.status])) as Record<number, LoanStatus>,
      )
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to load dashboard data.'
      notify(message, 'error')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadData()
  }, [loadData])

  useEffect(() => {
    if (users.length === 0) {
      setLoanUserId('')
      return
    }
    if (!loanUserId || !users.some((user) => String(user.id) === loanUserId)) {
      setLoanUserId(String(users[0].id))
    }
  }, [users, loanUserId])

  useEffect(() => {
    if (equipment.length === 0) {
      setLoanEquipmentId('')
      return
    }
    if (!loanEquipmentId || !equipment.some((item) => String(item.id) === loanEquipmentId)) {
      setLoanEquipmentId(String(equipment[0].id))
    }
  }, [equipment, loanEquipmentId])

  const openConfirm = (nextConfirm: ConfirmState) => setConfirmState(nextConfirm)
  const closeConfirm = () => setConfirmState(null)

  const submitEquipment = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!equipmentName.trim() || !equipmentType.trim()) {
      notify('Equipment name and type are required.', 'warning')
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
      notify('Equipment added to the catalog.', 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to create equipment.'
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const submitLoan = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const parsedUserId = Number(loanUserId)
    const parsedEquipmentId = Number(loanEquipmentId)

    if (Number.isNaN(parsedUserId) || parsedUserId <= 0 || Number.isNaN(parsedEquipmentId) || parsedEquipmentId <= 0) {
      notify('Choose both a user and an equipment item to create the loan.', 'warning')
      return
    }

    setBusyAction('create-loan')
    try {
      await loanService.create({ userId: parsedUserId, equipmentId: parsedEquipmentId })
      notify('Loan created successfully.', 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to create loan.'
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const submitUser = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!userName.trim() || !userEmail.trim()) {
      notify('Name and email are required to create a user.', 'warning')
      return
    }

    setBusyAction('create-user')
    try {
      await userService.create({
        name: userName.trim(),
        email: userEmail.trim(),
        password: userPassword.trim(),
        role: userRole,
      })
      setUserName('')
      setUserEmail('')
      setUserPassword('')
      setUserRole('USER')
      notify('User created and ready for access.', 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to create user.'
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const updateUser = async (user: User) => {
    const draft = userDrafts[user.id] ?? { name: user.name, email: user.email, role: user.role }
    if (!draft.name.trim() || !draft.email.trim()) {
      notify('User name and email are required.', 'warning')
      return
    }

    setBusyAction(`update-user-${user.id}`)
    try {
      await userService.update(user.id, { name: draft.name.trim(), email: draft.email.trim(), role: draft.role })
      notify(`User ${user.id} saved.`, 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to update user.'
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const deleteUser = async (id: number) => {
    setBusyAction(`delete-user-${id}`)
    try {
      await userService.remove(id)
      notify(`User ${id} deleted.`, 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to delete user.'
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const reserveEquipment = async (item: Equipment) => {
    setBusyAction(`reserve-equipment-${item.id}`)
    try {
      await equipmentService.reserve(item.id)
      notify(`Equipment ${item.id} moved to reserved.`, 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to reserve equipment.'
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const changeEquipmentStatus = async (item: Equipment, nextStatus: EquipmentStatus) => {
    setBusyAction(`status-equipment-${item.id}`)
    try {
      await equipmentService.update(item.id, { name: item.name, type: item.type, status: nextStatus })
      notify(`Equipment ${item.id} updated.`, 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to update equipment status.'
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const deleteEquipment = async (id: number) => {
    setBusyAction(`delete-equipment-${id}`)
    try {
      await equipmentService.remove(id)
      notify(`Equipment ${id} deleted.`, 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to delete equipment.'
      notify(message, 'error')
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
      notify(`Loan ${loan.id} saved.`, 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to update loan status.'
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const deleteLoan = async (id: number) => {
    setBusyAction(`delete-loan-${id}`)
    try {
      await loanService.remove(id)
      notify(`Loan ${id} deleted.`, 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to delete loan.'
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const sectionActions = (
    <Stack direction="row" spacing={1.5} flexWrap="wrap">
      <Button variant="outlined" onClick={() => void loadData()} disabled={loading || busyAction !== null}>
        Refresh
      </Button>
      <Button variant="outlined" color="inherit" onClick={logout} disabled={busyAction !== null}>
        Logout
      </Button>
    </Stack>
  )

  const renderEmptyRow = (message: string, colSpan: number) => (
    <TableRow>
      <TableCell colSpan={colSpan}>
        <Typography color="text.secondary">{message}</Typography>
      </TableCell>
    </TableRow>
  )

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Stack spacing={3}>
        <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" alignItems={{ md: 'center' }} gap={1.5}>
          <Box>
            <Typography variant="h3">Admin Operations</Typography>
            <Typography color="text.secondary">
              Welcome back, {session?.username}. Use the tabs to move between people, assets, and loan operations.
            </Typography>
          </Box>
          {sectionActions}
        </Stack>

        <Card>
          <CardContent>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.5} alignItems={{ md: 'center' }} justifyContent="space-between">
              <Box>
                <Typography variant="h6">Workspace at a glance</Typography>
              </Box>
              <Stack direction="row" spacing={1} flexWrap="wrap">
                <Chip label={`${stats.users} users`} color="primary" />
                <Chip label={`${stats.equipment} items`} color="secondary" />
                <Chip label={`${stats.available} available`} />
                <Chip label={`${stats.loans} loans`} />
              </Stack>
            </Stack>
          </CardContent>
        </Card>

        <Card>
          <CardContent sx={{ pb: 1 }}>
            <Tabs
              value={activeSection}
              onChange={(_, value: AdminSection) => setActiveSection(value)}
              variant="scrollable"
              scrollButtons="auto"
            >
              <SectionTab label="People" value="people" />
              <SectionTab label="Assets" value="assets" />
              <SectionTab label="Loans" value="loans" />
            </Tabs>
          </CardContent>
        </Card>

        {activeSection === 'people' ? (
          <Stack spacing={2}>
            <Card>
              <CardContent>
                <SectionHeader
                  title="Create User"
                  subtitle="Use a clear account setup flow and keep credentials visible only when needed."
                />
                <Stack component="form" spacing={1.5} onSubmit={submitUser} mt={2}>
                  <TextField value={userName} label="Name" onChange={(event) => setUserName(event.target.value)} />
                  <TextField value={userEmail} label="Email" onChange={(event) => setUserEmail(event.target.value)} />
                  <TextField
                    type="password"
                    value={userPassword}
                    label="Password"
                    onChange={(event) => setUserPassword(event.target.value)}
                    helperText="Leave blank only if your backend allows passwordless account creation."
                  />
                  <FormControl>
                    <InputLabel id="user-role-label">Role</InputLabel>
                    <Select
                      labelId="user-role-label"
                      value={userRole}
                      label="Role"
                      onChange={(event) => setUserRole(event.target.value as UserRole)}
                    >
                      {USER_ROLES.map((role) => (
                        <MenuItem value={role} key={role}>
                          {role}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <Button type="submit" variant="contained" disabled={busyAction === 'create-user'}>
                    {busyAction === 'create-user' ? 'Creating...' : 'Create user'}
                  </Button>
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <SectionHeader
                  title="Users"
                  subtitle="Edit records in place and confirm destructive changes before they happen."
                />
                <TableContainer sx={{ mt: 2 }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>ID</TableCell>
                        <TableCell>Name</TableCell>
                        <TableCell>Email</TableCell>
                        <TableCell>Role</TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {loading
                        ? renderEmptyRow('Loading users...', 5)
                        : users.length === 0
                          ? renderEmptyRow('No users exist yet. Create the first account from the form above.', 5)
                          : users.map((user) => {
                              const draft = userDrafts[user.id] ?? { name: user.name, email: user.email, role: user.role }
                              return (
                                <TableRow key={user.id}>
                                  <TableCell>{user.id}</TableCell>
                                  <TableCell>
                                    <TextField
                                      value={draft.name}
                                      size="small"
                                      onChange={(event) => {
                                        setUserDrafts((previous) => ({
                                          ...previous,
                                          [user.id]: { ...draft, name: event.target.value },
                                        }))
                                      }}
                                    />
                                  </TableCell>
                                  <TableCell>
                                    <TextField
                                      value={draft.email}
                                      size="small"
                                      onChange={(event) => {
                                        setUserDrafts((previous) => ({
                                          ...previous,
                                          [user.id]: { ...draft, email: event.target.value },
                                        }))
                                      }}
                                    />
                                  </TableCell>
                                  <TableCell>
                                    <FormControl size="small" sx={{ minWidth: 130 }}>
                                      <Select
                                        value={draft.role}
                                        onChange={(event) => {
                                          setUserDrafts((previous) => ({
                                            ...previous,
                                            [user.id]: { ...draft, role: event.target.value as UserRole },
                                          }))
                                        }}
                                      >
                                        {USER_ROLES.map((role) => (
                                          <MenuItem key={role} value={role}>
                                            {role}
                                          </MenuItem>
                                        ))}
                                      </Select>
                                    </FormControl>
                                  </TableCell>
                                  <TableCell>
                                    <Stack direction="row" spacing={1}>
                                      <Button size="small" variant="outlined" onClick={() => void updateUser(user)}>
                                        Save
                                      </Button>
                                      <Button
                                        size="small"
                                        color="error"
                                        variant="outlined"
                                        onClick={() =>
                                          openConfirm({
                                            title: `Delete user ${user.id}?`,
                                            description: 'This removes the user record permanently.',
                                            confirmLabel: 'Delete user',
                                            danger: true,
                                            action: async () => deleteUser(user.id),
                                          })
                                        }
                                      >
                                        Delete
                                      </Button>
                                    </Stack>
                                  </TableCell>
                                </TableRow>
                              )
                            })}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Stack>
        ) : null}

        {activeSection === 'assets' ? (
          <Stack spacing={2}>
            <Card>
              <CardContent>
                <SectionHeader
                  title="Create Equipment"
                  subtitle="Keep the asset catalog tidy and focused on what can be loaned."
                />
                <Stack component="form" spacing={1.5} onSubmit={submitEquipment} mt={2}>
                  <TextField value={equipmentName} label="Name" onChange={(event) => setEquipmentName(event.target.value)} />
                  <TextField value={equipmentType} label="Type" onChange={(event) => setEquipmentType(event.target.value)} />
                  <FormControl>
                    <InputLabel id="equipment-status-label">Status</InputLabel>
                    <Select
                      labelId="equipment-status-label"
                      value={equipmentStatus}
                      label="Status"
                      onChange={(event) => setEquipmentStatus(event.target.value as EquipmentStatus)}
                    >
                      {EQUIPMENT_STATUSES.map((status) => (
                        <MenuItem value={status} key={status}>
                          {status}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <Button type="submit" variant="contained" disabled={busyAction === 'create-equipment'}>
                    {busyAction === 'create-equipment' ? 'Creating...' : 'Create equipment'}
                  </Button>
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <SectionHeader
                  title="Equipment"
                  subtitle="Reserve available items or update their status in a single place."
                />
                <TableContainer sx={{ mt: 2 }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>ID</TableCell>
                        <TableCell>Name</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {loading
                        ? renderEmptyRow('Loading equipment...', 5)
                        : equipment.length === 0
                          ? renderEmptyRow('No equipment exists yet. Add the first item above.', 5)
                          : equipment.map((item) => (
                              <TableRow key={item.id}>
                                <TableCell>{item.id}</TableCell>
                                <TableCell>{item.name}</TableCell>
                                <TableCell>{item.type}</TableCell>
                                <TableCell>
                                  <Chip size="small" label={item.status} />
                                </TableCell>
                                <TableCell>
                                  <Stack direction="row" spacing={1} flexWrap="wrap">
                                    <Button
                                      size="small"
                                      variant="outlined"
                                      disabled={item.status !== 'AVAILABLE' || busyAction === `reserve-equipment-${item.id}`}
                                      onClick={() => void reserveEquipment(item)}
                                    >
                                      Reserve
                                    </Button>
                                    <FormControl size="small" sx={{ minWidth: 180 }}>
                                      <Select
                                        defaultValue={item.status}
                                        onChange={(event) => {
                                          void changeEquipmentStatus(item, event.target.value as EquipmentStatus)
                                        }}
                                      >
                                        {EQUIPMENT_STATUSES.map((status) => (
                                          <MenuItem key={status} value={status}>
                                            {status}
                                          </MenuItem>
                                        ))}
                                      </Select>
                                    </FormControl>
                                    <Button
                                      size="small"
                                      color="error"
                                      variant="outlined"
                                      onClick={() =>
                                        openConfirm({
                                          title: `Delete equipment ${item.id}?`,
                                          description: 'This removes the item from the catalog and cannot be undone.',
                                          confirmLabel: 'Delete equipment',
                                          danger: true,
                                          action: async () => deleteEquipment(item.id),
                                        })
                                      }
                                    >
                                      Delete
                                    </Button>
                                  </Stack>
                                </TableCell>
                              </TableRow>
                            ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Stack>
        ) : null}

        {activeSection === 'loans' ? (
          <Stack spacing={2}>
            <Card>
              <CardContent>
                <SectionHeader
                  title="Create Loan"
                  subtitle="Connect a user to available equipment without leaving the loan workspace."
                />
                <Stack component="form" spacing={1.5} onSubmit={submitLoan} mt={2}>
                  <FormControl>
                    <InputLabel id="loan-user-label">User</InputLabel>
                    <Select
                      labelId="loan-user-label"
                      value={loanUserId}
                      label="User"
                      onChange={(event) => setLoanUserId(event.target.value)}
                    >
                      {users.map((user) => (
                        <MenuItem key={user.id} value={String(user.id)}>
                          {user.id} - {user.name}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <FormControl>
                    <InputLabel id="loan-equipment-label">Equipment</InputLabel>
                    <Select
                      labelId="loan-equipment-label"
                      value={loanEquipmentId}
                      label="Equipment"
                      onChange={(event) => setLoanEquipmentId(event.target.value)}
                    >
                      {equipment.map((item) => (
                        <MenuItem key={item.id} value={String(item.id)}>
                          {item.id} - {item.name}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <Button type="submit" variant="contained" disabled={busyAction === 'create-loan'}>
                    {busyAction === 'create-loan' ? 'Creating...' : 'Create loan'}
                  </Button>
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <SectionHeader
                  title="Loans"
                  subtitle="Track current and historical loans with a focused, review-friendly table."
                />
                <TableContainer sx={{ mt: 2 }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>ID</TableCell>
                        <TableCell>User</TableCell>
                        <TableCell>Equipment</TableCell>
                        <TableCell>Equipment Name</TableCell>
                        <TableCell>Date</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {loading
                        ? renderEmptyRow('Loading loans...', 7)
                        : loans.length === 0
                          ? renderEmptyRow('No loans have been created yet.', 7)
                          : loans.map((loan) => {
                              const relatedEquipment = equipmentById.get(loan.equipmentId)
                              const selectedStatus = loanStatusDrafts[loan.id] ?? loan.status
                              return (
                                <TableRow key={loan.id}>
                                  <TableCell>{loan.id}</TableCell>
                                  <TableCell>{loan.userId}</TableCell>
                                  <TableCell>{loan.equipmentId}</TableCell>
                                  <TableCell>{relatedEquipment ? relatedEquipment.name : '-'}</TableCell>
                                  <TableCell>{formatDateTime(loan.loanDate)}</TableCell>
                                  <TableCell>{loan.status}</TableCell>
                                  <TableCell>
                                    <Stack direction="row" spacing={1} flexWrap="wrap">
                                      <FormControl size="small" sx={{ minWidth: 140 }}>
                                        <Select
                                          value={selectedStatus}
                                          onChange={(event) => {
                                            setLoanStatusDrafts((previous) => ({
                                              ...previous,
                                              [loan.id]: event.target.value as LoanStatus,
                                            }))
                                          }}
                                        >
                                          {LOAN_STATUSES.map((status) => (
                                            <MenuItem key={status} value={status}>
                                              {status}
                                            </MenuItem>
                                          ))}
                                        </Select>
                                      </FormControl>
                                      <Button size="small" variant="outlined" onClick={() => void updateLoanStatus(loan)}>
                                        Save
                                      </Button>
                                      <Button
                                        size="small"
                                        color="error"
                                        variant="outlined"
                                        onClick={() =>
                                          openConfirm({
                                            title: `Delete loan ${loan.id}?`,
                                            description: 'This permanently removes the loan record.',
                                            confirmLabel: 'Delete loan',
                                            danger: true,
                                            action: async () => deleteLoan(loan.id),
                                          })
                                        }
                                      >
                                        Delete
                                      </Button>
                                    </Stack>
                                  </TableCell>
                                </TableRow>
                              )
                            })}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Stack>
        ) : null}
      </Stack>

      <AppSnackbar
        open={snackbar !== null}
        message={snackbar?.message ?? ''}
        severity={snackbar?.severity ?? 'info'}
        onClose={() => setSnackbar(null)}
      />

      <ConfirmDialog
        open={confirmState !== null}
        title={confirmState?.title ?? ''}
        description={confirmState?.description ?? ''}
        confirmLabel={confirmState?.confirmLabel ?? 'Confirm'}
        danger={confirmState?.danger ?? false}
        onClose={closeConfirm}
        onConfirm={async () => {
          if (!confirmState) {
            return
          }
          const action = confirmState.action
          closeConfirm()
          await action()
        }}
      />
    </Container>
  )
}
