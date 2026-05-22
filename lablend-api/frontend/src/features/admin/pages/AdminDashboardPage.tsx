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
  Autocomplete,
  List,
  ListItem,
  ListItemText,
  Divider,
  Avatar,
} from '@mui/material'
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty'
import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { equipmentService } from '../../../services/equipmentService'
import { loanService } from '../../../services/loanService'
import { userService } from '../../../services/userService'
import { waitingListService } from '../../../services/waitingListService' 
import { useAuth } from '../../auth/context/AuthContext'
import type { Equipment, EquipmentStatus, Loan, LoanStatus, User, UserRole, WaitingList } from '../../../shared/types/domain'
import { AppSnackbar } from '../../../shared/ui/AppSnackbar'
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog'
import { SectionHeader } from '../../../shared/ui/SectionHeader'
import { LanguageSwitcher } from '../../../shared/i18n/LanguageSwitcher'
import { useI18n } from '../../../shared/i18n/I18nContext'

const EQUIPMENT_STATUSES: EquipmentStatus[] = ['AVAILABLE', 'RESERVED', 'UNDER_MAINTENANCE']
const LOAN_STATUSES: LoanStatus[] = ['ACTIVE', 'COMPLETED', 'CANCELLED']
const USER_ROLES: UserRole[] = ['ADMIN', 'USER']

type AdminSection = 'people' | 'assets' | 'loans' | 'flagged' | 'waiting'

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

type HttpErrorLike = {
  response?: {
    status?: number
    data?: string
  }
}

const getHttpError = (error: unknown): HttpErrorLike =>
  typeof error === 'object' && error !== null ? (error as HttpErrorLike) : {}

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
  const { t } = useI18n()
  const [activeSection, setActiveSection] = useState<AdminSection>('people')

  const [equipment, setEquipment] = useState<Equipment[]>([])
  const [loans, setLoans] = useState<Loan[]>([])
  const [users, setUsers] = useState<User[]>([])

  const [flaggedUsers, setFlaggedUsers] = useState<User[]>([])
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

  const [selectedWaitingEquipmentId, setSelectedWaitingEquipmentId] = useState<string | null>(null)
  const [currentQueue, setCurrentQueue] = useState<WaitingList[]>([])
  const [loadingQueue, setLoadingQueue] = useState(false)

  const equipmentById = useMemo(() => new Map(equipment.map((item) => [item.id, item])), [equipment])
  const stats = useMemo(
    () => ({
      users: users.length,
      equipment: equipment.length,
      available: equipment.filter((item) => item.status === 'AVAILABLE').length,
      loans: loans.length,
      flagged: flaggedUsers.length,
    }),
    [equipment, loans, users, flaggedUsers],
  )

  const selectedWaitingItem = useMemo(
    () => equipment.find((item) => String(item.id) === selectedWaitingEquipmentId) ?? null,
    [equipment, selectedWaitingEquipmentId],
  )

  const notify = useCallback((message: string, severity: SnackbarState['severity']) => {
    setSnackbar({ message, severity })
  }, [])

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const [userResponse, equipmentResponse, loanResponse, flaggedResponse] = await Promise.all([
        userService.getAll(),
        equipmentService.getAll(),
        loanService.getAll(),
        userService.getFlagged(),
      ])
      setUsers(userResponse)
      setEquipment(equipmentResponse)
      setLoans(loanResponse)
      setFlaggedUsers(flaggedResponse)
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
      const message = error instanceof Error ? error.message : t('admin.notify.loadFailed')
      notify(message, 'error')
    } finally {
      setLoading(false)
    }
  }, [notify, t])

  useEffect(() => {
    void loadData()
  }, [loadData])

  useEffect(() => {
    const loadQueue = async () => {
      if (!selectedWaitingEquipmentId) {
        setCurrentQueue([])
        return
      }
      setLoadingQueue(true)
      try {
        const response = await waitingListService.getQueueForEquipment(Number(selectedWaitingEquipmentId))
        const sorted = response.sort(
          (a, b) => new Date(a.requestDate).getTime() - new Date(b.requestDate).getTime()
        )
        setCurrentQueue(sorted)
      } catch {
        notify(t('admin.notify.queueFailed'), 'error')
      } finally {
        setLoadingQueue(false)
      }
    }

    void loadQueue()
  }, [notify, selectedWaitingEquipmentId, t])

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

  const handleUnblockUser = async (id: number) => {
    setBusyAction(`unblock-user-${id}`)
    try {
      await userService.unblock(id)
      notify(t('admin.notify.unblocked'), 'success')
    } catch (error: unknown) {
      const httpError = getHttpError(error)
      if (httpError.response?.status === 200 || !httpError.response) {
        notify(t('admin.notify.unblocked'), 'success')
      } else {
        notify(httpError.response?.data || t('admin.notify.unblockFailed'), 'error')
      }
    } finally {
      await loadData()
      setBusyAction(null)
    }
  }

  const handleBlockUser = async (id: number) => {
    setBusyAction(`block-user-${id}`)
    try {
      await userService.block(id)
      notify(t('admin.notify.blocked'), 'success')
    } catch (error: unknown) {
      const httpError = getHttpError(error)
      if (httpError.response?.status === 200 || !httpError.response) {
        notify(t('admin.notify.blocked'), 'success')
      } else {
        notify(httpError.response?.data || t('admin.notify.blockFailed'), 'error')
      }
    } finally {
      await loadData()
      setBusyAction(null)
    }
  }

  const submitEquipment = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!equipmentName.trim() || !equipmentType.trim()) {
      notify(t('admin.notify.equipmentRequired'), 'warning')
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
      notify(t('admin.notify.equipmentCreated'), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.equipmentCreateFailed')
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
      notify(t('admin.notify.loanRequired'), 'warning')
      return
    }

    setBusyAction('create-loan')
    try {
      await loanService.create({ userId: parsedUserId, equipmentId: parsedEquipmentId })
      notify(t('admin.notify.loanCreated'), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.loanCreateFailed')
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const submitUser = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!userName.trim() || !userEmail.trim()) {
      notify(t('admin.notify.userRequired'), 'warning')
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
      notify(t('admin.notify.userCreated'), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.userCreateFailed')
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const updateUser = async (user: User) => {
    const draft = userDrafts[user.id] ?? { name: user.name, email: user.email, role: user.role }
    if (!draft.name.trim() || !draft.email.trim()) {
      notify(t('admin.notify.userUpdateRequired'), 'warning')
      return
    }

    setBusyAction(`update-user-${user.id}`)
    try {
      await userService.update(user.id, { name: draft.name.trim(), email: draft.email.trim(), role: draft.role })
      notify(t('admin.notify.userSaved', { id: user.id }), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.userUpdateFailed')
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const deleteUser = async (id: number) => {
    setBusyAction(`delete-user-${id}`)
    try {
      await userService.remove(id)
      notify(t('admin.notify.userDeleted', { id }), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.userDeleteFailed')
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const reserveEquipment = async (item: Equipment) => {
    setBusyAction(`reserve-equipment-${item.id}`)
    try {
      await equipmentService.reserve(item.id)
      notify(t('admin.notify.equipmentReserved', { id: item.id }), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.equipmentReserveFailed')
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const changeEquipmentStatus = async (item: Equipment, nextStatus: EquipmentStatus) => {
    setBusyAction(`status-equipment-${item.id}`)
    try {
      await equipmentService.update(item.id, { name: item.name, type: item.type, status: nextStatus })
      notify(t('admin.notify.equipmentUpdated', { id: item.id }), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.equipmentUpdateFailed')
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const deleteEquipment = async (id: number) => {
    setBusyAction(`delete-equipment-${id}`)
    try {
      await equipmentService.remove(id)
      notify(t('admin.notify.equipmentDeleted', { id }), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.equipmentDeleteFailed')
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
      notify(t('admin.notify.loanSaved', { id: loan.id }), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.loanUpdateFailed')
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const deleteLoan = async (id: number) => {
    setBusyAction(`delete-loan-${id}`)
    try {
      await loanService.remove(id)
      notify(t('admin.notify.loanDeleted', { id }), 'success')
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('admin.notify.loanDeleteFailed')
      notify(message, 'error')
    } finally {
      setBusyAction(null)
    }
  }

  const sectionActions = (
    <Stack direction="row" spacing={1.5} flexWrap="wrap">
      <Button variant="outlined" onClick={() => void loadData()} disabled={loading || busyAction !== null}>
        {t('common.refresh')}
      </Button>
      <Button variant="outlined" color="inherit" onClick={logout} disabled={busyAction !== null}>
        {t('common.logout')}
      </Button>
      <LanguageSwitcher />
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
            <Typography variant="h3">{t('admin.title')}</Typography>
            <Typography color="text.secondary">
              {t('admin.subtitle', { username: session?.username ?? '' })}
            </Typography>
          </Box>
          {sectionActions}
        </Stack>

        <Card>
          <CardContent>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.5} alignItems={{ md: 'center' }} justifyContent="space-between">
              <Box>
                <Typography variant="h6">{t('admin.glance')}</Typography>
              </Box>
              <Stack direction="row" spacing={1} flexWrap="wrap">
                <Chip label={t('admin.stats.users', { count: stats.users })} color="primary" />
                <Chip label={t('admin.stats.items', { count: stats.equipment })} color="secondary" />
                <Chip label={t('admin.stats.available', { count: stats.available })} />
                <Chip label={t('admin.stats.loans', { count: stats.loans })} />
                <Chip label={t('admin.stats.flagged', { count: stats.flagged })} color="error" />
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
              <SectionTab label={t('admin.tabs.people')} value="people" />
              <SectionTab label={t('admin.tabs.assets')} value="assets" />
              <SectionTab label={t('admin.tabs.loans')} value="loans" />
              <SectionTab label={t('admin.tabs.flagged')} value="flagged" />
              <SectionTab label={t('admin.tabs.waiting')} value="waiting" />
            </Tabs>
          </CardContent>
        </Card>

        {activeSection === 'people' ? (
          <Stack spacing={2}>
            <Card>
              <CardContent>
                <SectionHeader
                  title={t('admin.createUser.title')}
                  subtitle={t('admin.createUser.subtitle')}
                />
                <Stack component="form" spacing={1.5} onSubmit={submitUser} mt={2}>
                  <TextField value={userName} label={t('common.name')} onChange={(event) => setUserName(event.target.value)} />
                  <TextField value={userEmail} label={t('common.email')} onChange={(event) => setUserEmail(event.target.value)} />
                  <TextField
                    type="password"
                    value={userPassword}
                    label={t('common.password')}
                    onChange={(event) => setUserPassword(event.target.value)}
                    helperText={t('admin.createUser.passwordHelp')}
                  />
                  <FormControl>
                    <InputLabel id="user-role-label">{t('common.role')}</InputLabel>
                    <Select
                      labelId="user-role-label"
                      value={userRole}
                      label={t('common.role')}
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
                    {busyAction === 'create-user' ? t('admin.createUser.creating') : t('admin.createUser.submit')}
                  </Button>
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <SectionHeader
                  title={t('admin.users.title')}
                  subtitle={t('admin.users.subtitle')}
                />
                <TableContainer sx={{ mt: 2 }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>{t('common.id')}</TableCell>
                        <TableCell>{t('common.name')}</TableCell>
                        <TableCell>{t('common.email')}</TableCell>
                        <TableCell>{t('common.role')}</TableCell>
                        <TableCell>{t('common.actions')}</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {loading
                        ? renderEmptyRow(t('admin.users.loading'), 5)
                        : users.length === 0
                          ? renderEmptyRow(t('admin.users.empty'), 5)
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
                                    {user.status !== 'BLOCKED' && user.role === 'USER' && (
                                      <Button
                                        size="small"
                                        variant="contained"
                                        color="warning"
                                        disabled={busyAction !== null}
                                        onClick={() =>
                                          openConfirm({
                                            title: `¿Bloquear usuario ${user.id}?`,
                                            description: `This will temporarily suspend ${user.name} and move them to the sanctioned tab.`,
                                            confirmLabel: 'Block user',
                                            danger: true,
                                            action: async () => handleBlockUser(user.id),
                                          })
                                        }
                                      >
                                        Block
                                      </Button>
                                    )}

                                    <Button
                                      size="small"
                                      color="error"
                                      variant="outlined"
                                      onClick={() =>
                                        openConfirm({
                                          title: t('admin.users.deleteTitle', { id: user.id }),
                                          description: t('admin.users.deleteDescription'),
                                          confirmLabel: t('admin.users.deleteConfirm'),
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
                  title={t('admin.equipmentCenter.title')}
                  subtitle={t('admin.equipmentCenter.subtitle')}
                />
                <Stack component="form" spacing={1.5} onSubmit={submitEquipment} mt={2}>
                  <TextField value={equipmentName} label={t('common.name')} onChange={(event) => setEquipmentName(event.target.value)} />
                  <TextField value={equipmentType} label={t('common.type')} onChange={(event) => setEquipmentType(event.target.value)} />
                  <FormControl>
                    <InputLabel id="equipment-status-label">{t('common.status')}</InputLabel>
                    <Select
                      labelId="equipment-status-label"
                      value={equipmentStatus}
                      label={t('common.status')}
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
                    {busyAction === 'create-equipment' ? t('admin.createUser.creating') : t('admin.equipmentCenter.submit')}
                  </Button>
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <SectionHeader
                  title={t('admin.equipment.title')}
                  subtitle={t('admin.equipment.subtitle')}
                />
                <TableContainer sx={{ mt: 2 }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>{t('common.id')}</TableCell>
                        <TableCell>{t('common.name')}</TableCell>
                        <TableCell>{t('common.type')}</TableCell>
                        <TableCell>{t('common.status')}</TableCell>
                        <TableCell>{t('common.actions')}</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {loading
                        ? renderEmptyRow(t('admin.equipment.loading'), 5)
                        : equipment.length === 0
                          ? renderEmptyRow(t('admin.equipment.empty'), 5)
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
                                      {t('admin.equipment.reserve')}
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
                                          title: t('admin.equipment.deleteTitle', { id: item.id }),
                                          description: t('admin.equipment.deleteDescription'),
                                          confirmLabel: t('admin.equipment.deleteConfirm'),
                                          danger: true,
                                          action: async () => deleteEquipment(item.id),
                                        })
                                      }
                                    >
                                      {t('common.delete')}
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
                  title={t('admin.loans.createTitle')}
                  subtitle={t('admin.loans.createSubtitle')}
                />
                <Stack component="form" spacing={1.5} onSubmit={submitLoan} mt={2}>
                  <FormControl>
                    <InputLabel id="loan-user-label">{t('common.user')}</InputLabel>
                    <Select
                      labelId="loan-user-label"
                      value={loanUserId}
                      label={t('common.user')}
                      onChange={(event) => setLoanUserId(event.target.value)}
                    >
                      {users.map((user) => (
                        <MenuItem key={user.id} value={String(user.id)}>
                          {user.id} - {user.name}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <Autocomplete
                    options={equipment.filter((e) => e.status === 'AVAILABLE')}
                    getOptionLabel={(option) => `${option.id} - ${option.name}`}
                    isOptionEqualToValue={(option, value) => option.id === value.id}
                    value={equipment.find((item) => String(item.id) === loanEquipmentId) ?? null}
                    onChange={(_, value) => setLoanEquipmentId(value ? String(value.id) : '')}
                    size="small"
                    fullWidth
                    loading={loading}
                    noOptionsText={loading ? t('admin.equipment.loading') : t('user.equipment.noAvailable')}
                    renderInput={(params) => <TextField {...params} label={t('common.equipment')} />}
                  />
                  <Button type="submit" variant="contained" disabled={busyAction === 'create-loan'}>
                    {busyAction === 'create-loan' ? t('admin.loans.creating') : t('admin.loans.submit')}
                  </Button>
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <SectionHeader
                  title={t('admin.loans.title')}
                  subtitle={t('admin.loans.subtitle')}
                />
                <TableContainer sx={{ mt: 2 }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>{t('common.id')}</TableCell>
                        <TableCell>{t('common.user')}</TableCell>
                        <TableCell>{t('common.equipment')}</TableCell>
                        <TableCell>{t('admin.loans.equipmentName')}</TableCell>
                        <TableCell>{t('common.date')}</TableCell>
                        <TableCell>{t('common.status')}</TableCell>
                        <TableCell>{t('common.actions')}</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {loading
                        ? renderEmptyRow(t('admin.loans.loading'), 7)
                        : loans.length === 0
                          ? renderEmptyRow(t('admin.loans.empty'), 7)
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
                                        {t('common.save')}
                                      </Button>
                                      <Button
                                        size="small"
                                        color="error"
                                        variant="outlined"
                                        onClick={() =>
                                          openConfirm({
                                            title: t('admin.loans.deleteTitle', { id: loan.id }),
                                            description: t('admin.loans.deleteDescription'),
                                            confirmLabel: t('admin.loans.deleteConfirm'),
                                            danger: true,
                                            action: async () => deleteLoan(loan.id),
                                          })
                                        }
                                      >
                                        {t('common.delete')}
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

        {activeSection === 'flagged' ? (
          <Stack spacing={2}>
            <Card>
              <CardContent>
                <SectionHeader
                  title={t('admin.flagged.title')}
                  subtitle={t('admin.flagged.subtitle')}
                />
                <TableContainer sx={{ mt: 2 }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>{t('common.id')}</TableCell>
                        <TableCell>{t('common.name')}</TableCell>
                        <TableCell>{t('common.email')}</TableCell>
                        <TableCell>{t('admin.flagged.sanctions')}</TableCell>
                        <TableCell>{t('admin.flagged.severity')}</TableCell>
                        <TableCell>{t('common.actions')}</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {loading ? (
                        renderEmptyRow(t('admin.flagged.loading'), 6)
                      ) : flaggedUsers.length === 0 ? (
                        renderEmptyRow(t('admin.flagged.empty'), 6)
                      ) : (
                        flaggedUsers.map((user) => {
                          const needsManualReview = !user.requiresManualReview

                          return (
                            <TableRow key={user.id}>
                              <TableCell>{user.id}</TableCell>
                              <TableCell>{user.name}</TableCell>
                              <TableCell>{user.email}</TableCell>
                              <TableCell>{t('admin.flagged.sanctionsDetail', { count: user.penaltyCount ?? 0 })}</TableCell>
                              <TableCell>
                                {needsManualReview ? (
                                  <Chip size="small" label={t('admin.flagged.manual')} color="error" variant="filled" sx={{ fontWeight: 'bold' }} />
                                ) : (
                                  <Chip size="small" label={t('admin.flagged.temporary')} color="warning" variant="outlined" />
                                )}
                              </TableCell>
                              <TableCell>
                                <Button
                                  size="small"
                                  variant="contained"
                                  color="success"
                                  disabled={busyAction !== null}
                                  onClick={() => void handleUnblockUser(user.id)}
                                >
                                  {t('admin.flagged.unblock')}
                                </Button>
                              </TableCell>
                            </TableRow>
                          )
                        })
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Stack>
        ) : null}

        {activeSection === 'waiting' ? (
          <Stack spacing={2}>
            <Card>
              <CardContent>
                <SectionHeader
                  title={t('admin.waiting.title')}
                  subtitle={t('admin.waiting.subtitle')}
                />
                <Box mt={3}>
                  <Autocomplete
                    options={equipment}
                    getOptionLabel={(option) => `${option.id} - ${option.name} (${option.status})`}
                    isOptionEqualToValue={(option, value) => option.id === value.id}
                    value={selectedWaitingItem}
                    onChange={(_, value) => setSelectedWaitingEquipmentId(value ? String(value.id) : null)}
                    size="small"
                    fullWidth
                    noOptionsText={t('admin.waiting.noEquipment')}
                    renderInput={(params) => <TextField {...params} label={t('admin.waiting.select')} />}
                  />
                </Box>
              </CardContent>
            </Card>

            {selectedWaitingItem ? (
              <Card>
                <CardContent>
                  <Stack spacing={2}>
                    <Box display="flex" justifyContent="space-between" alignItems="center">
                      <Typography variant="h6">
                        {t('admin.waiting.queueFor')}{' '}
                        <span style={{ color: '#1976d2', fontWeight: 'bold' }}>
                          {selectedWaitingItem.name}
                        </span>
                      </Typography>
                      <Chip
                        label={t('admin.waiting.studentsWaiting', { count: currentQueue.length })}
                        color={currentQueue.length > 0 ? 'primary' : 'default'}
                        size="small"
                      />
                    </Box>

                    <Divider />

                    {loadingQueue ? (
                      <Typography color="text.secondary" sx={{ py: 2 }}>
                        {t('admin.waiting.fetching')}
                      </Typography>
                    ) : currentQueue.length === 0 ? (
                      <Stack direction="row" spacing={1} alignItems="center" sx={{ py: 3, justifyContent: 'center' }}>
                        <HourglassEmptyIcon color="disabled" />
                        <Typography color="text.secondary">
                          {t('admin.waiting.empty')}
                        </Typography>
                      </Stack>
                    ) : (
                      <List disablePadding>
                        {currentQueue.map((entry, index) => (
                          <Stack key={entry.id}>
                            {index > 0 ? <Divider component="li" /> : null}
                            <ListItem disableGutters>
                              <Avatar
                                sx={{
                                  width: 28,
                                  height: 28,
                                  fontSize: '0.875rem',
                                  mr: 2,
                                  bgcolor: index === 0 ? 'success.main' : 'action.selected',
                                  color: index === 0 ? 'white' : 'text.primary',
                                }}
                              >
                                {index + 1}
                              </Avatar>

                              <ListItemText
                                primary={t('admin.waiting.studentId', { id: entry.userId })}
                                secondary={t('admin.waiting.since', { date: formatDateTime(entry.requestDate) })}
                              />

                              {index === 0 && (
                                <Chip label={t('admin.waiting.next')} color="success" size="small" variant="outlined" />
                              )}
                            </ListItem>
                          </Stack>
                        ))}
                      </List>
                    )}
                  </Stack>
                </CardContent>
              </Card>
            ) : (
              <Card variant="outlined" sx={{ borderStyle: 'dashed', backgroundColor: 'background.default' }}>
                <CardContent sx={{ py: 5, textAlign: 'center' }}>
                  <Typography color="text.secondary">
                    {t('admin.waiting.choose')}
                  </Typography>
                </CardContent>
              </Card>
            )}
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
