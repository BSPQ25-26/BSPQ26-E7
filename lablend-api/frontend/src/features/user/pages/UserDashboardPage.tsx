import {
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Divider,
  List,
  ListItem,
  ListItemText,
  Stack,
  Typography,
  Autocomplete,
  TextField,
  Chip,
} from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { equipmentService } from '../../../services/equipmentService'
import { loanService } from '../../../services/loanService'
import { waitingListService } from '../../../services/waitingListService'
import { useAuth } from '../../auth/context/AuthContext'
import type { Equipment, Loan } from '../../../shared/types/domain'
import { AppSnackbar } from '../../../shared/ui/AppSnackbar'
import { SectionHeader } from '../../../shared/ui/SectionHeader'
import { LanguageSwitcher } from '../../../shared/i18n/LanguageSwitcher'
import { useI18n } from '../../../shared/i18n/I18nContext'

export const UserDashboardPage = () => {
  const { session, logout } = useAuth()
  const { t } = useI18n()
  const [equipment, setEquipment] = useState<Equipment[]>([])
  const [loans, setLoans] = useState<Loan[]>([])
  const [selectedEquipmentId, setSelectedEquipmentId] = useState<string | null>(null)
  const [busyAction, setBusyAction] = useState<string | null>(null)
  const [snackbar, setSnackbar] = useState<{
    message: string
    severity: 'success' | 'error' | 'info' | 'warning'
  } | null>(null)

  const selectedItem = useMemo(
    () => equipment.find((item) => String(item.id) === selectedEquipmentId) ?? null,
    [equipment, selectedEquipmentId],
  )

  const activeUserLoans = useMemo(
    () => loans.filter((loan) => loan.userId === session?.userId && loan.status === 'ACTIVE'),
    [loans, session?.userId],
  )

  const isAlreadyLoanedByMe = useMemo(() => {
    if (!selectedItem || !session?.userId) return false
    return loans.some(
      (loan) =>
        loan.equipmentId === selectedItem.id &&
        loan.userId === session.userId &&
        loan.status === 'ACTIVE'
    )
  }, [loans, selectedItem, session?.userId])

  const equipmentById = useMemo(
    () => new Map(equipment.map((item) => [item.id, item])),
    [equipment],
  )

  const loadData = useCallback(async () => {
    try {
      const [equipmentResponse, loansResponse] = await Promise.all([
        equipmentService.getAll(),
        loanService.getAll(),
      ])
      setEquipment(equipmentResponse)
      setLoans(loansResponse)
      
      if (equipmentResponse.length > 0 && !selectedEquipmentId) {
        setSelectedEquipmentId(String(equipmentResponse[0].id))
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : t('user.notify.loadFailed')
      setSnackbar({ message, severity: 'error' })
    }
  }, [selectedEquipmentId, t])

  useEffect(() => {
    void loadData()
  }, [loadData])

  const submitLoan = async () => {
    if (!session || !selectedItem) return

    setBusyAction('loan')
    try {
      await loanService.create({
        userId: session.userId,
        equipmentId: selectedItem.id,
      })
      setSnackbar({ message: t('user.notify.loanCreated'), severity: 'success' })
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('user.notify.loanFailed')
      setSnackbar({ message, severity: 'error' })
    } finally {
      setBusyAction(null)
    }
  }

  const handleJoinWaitingList = async () => {
    if (!session || !selectedItem || isAlreadyLoanedByMe) return

    setBusyAction('waiting')
    try {
      await waitingListService.join({
        userId: session.userId,
        equipmentId: selectedItem.id,
      })
      setSnackbar({ 
        message: t('user.notify.joined', { name: selectedItem.name }), 
        severity: 'success' 
      })
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('user.notify.joinFailed')
      setSnackbar({ message, severity: 'error' })
    } finally {
      setBusyAction(null)
    }
  }

  const returnLoan = async (loanId: number) => {
    setBusyAction(`return-${loanId}`)
    try {
      await loanService.returnLoan(loanId)
      setSnackbar({ message: t('user.notify.returned'), severity: 'success' })
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : t('user.notify.returnFailed')
      setSnackbar({ message, severity: 'error' })
    } finally {
      setBusyAction(null)
    }
  }

  return (
    <Container maxWidth="md" sx={{ py: 6 }}>
      <Stack spacing={3}>
        <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ sm: 'flex-start' }} gap={2}>
          <Box>
            <Typography variant="h3">{t('user.title', { username: session?.username ?? '' })}</Typography>
            <Typography color="text.secondary">{t('user.subtitle')}</Typography>
          </Box>
          <LanguageSwitcher />
        </Stack>

        <Card>
          <CardContent>
            <Stack spacing={2}>
              <SectionHeader
                title={t('user.equipment.title')}
                subtitle={t('user.equipment.subtitle')}
              />

              {equipment.length === 0 ? (
                <Card variant="outlined" sx={{ backgroundColor: 'background.default' }}>
                  <CardContent>
                    <Stack spacing={1}>
                      <Typography variant="h6">{t('user.equipment.emptyTitle')}</Typography>
                      <Typography color="text.secondary">{t('user.equipment.emptySubtitle')}</Typography>
                    </Stack>
                  </CardContent>
                </Card>
              ) : (
                <>
                  <Autocomplete
                    options={equipment}
                    getOptionLabel={(option) => `${option.id} - ${option.name} (${option.status})`}
                    isOptionEqualToValue={(option, value) => option.id === value.id}
                    value={selectedItem}
                    onChange={(_, value) => setSelectedEquipmentId(value ? String(value.id) : null)}
                    size="small"
                    fullWidth
                    noOptionsText={t('user.equipment.noOptions')}
                    renderInput={(params) => <TextField {...params} label={t('user.equipment.select')} />}
                  />

                  {selectedItem && selectedItem.status === 'AVAILABLE' ? (
                    <Button
                      variant="contained"
                      onClick={() => void submitLoan()}
                      disabled={busyAction === 'loan'}
                      fullWidth
                    >
                      {busyAction === 'loan' ? t('user.equipment.creatingLoan') : t('user.equipment.borrow')}
                    </Button>
                  ) : selectedItem ? (
                    <Stack spacing={1} width="100%">
                      <Chip 
                        label={
                          isAlreadyLoanedByMe 
                            ? t('user.equipment.activeBorrow') 
                            : selectedItem.status === 'UNDER_MAINTENANCE'
                            ? t('user.equipment.maintenance')
                            : t('user.equipment.loaned')
                        } 
                        color={isAlreadyLoanedByMe ? "error" : selectedItem.status === 'UNDER_MAINTENANCE' ? "info" : "warning"} 
                        variant="outlined" 
                      />
                      <Button
                        variant="contained"
                        color={selectedItem.status === 'UNDER_MAINTENANCE' ? "primary" : "secondary"}
                        onClick={() => void handleJoinWaitingList()}
                        disabled={busyAction === 'waiting' || isAlreadyLoanedByMe}
                        fullWidth
                      >
                        {isAlreadyLoanedByMe 
                          ? t('user.equipment.cannotJoin') 
                          : busyAction === 'waiting' 
                          ? t('user.equipment.joining') 
                          : t('user.equipment.join')}
                      </Button>
                    </Stack>
                  ) : null}
                </>
              )}
            </Stack>
          </CardContent>
        </Card>

        <Card>
          <CardContent>
            <Stack spacing={2}>
              <SectionHeader
                title={t('user.active.title')}
                subtitle={t('user.active.subtitle')}
              />

              {activeUserLoans.length === 0 ? (
                <Typography color="text.secondary">
                  {t('user.active.empty')}
                </Typography>
              ) : (
                <List disablePadding>
                  {activeUserLoans.map((loan, index) => {
                    const borrowedEquipment = equipmentById.get(loan.equipmentId)

                    return (
                      <Stack key={loan.id} spacing={0}>
                        {index > 0 ? <Divider component="li" /> : null}
                        <ListItem
                          disableGutters
                          secondaryAction={
                            <Button
                              variant="outlined"
                              onClick={() => void returnLoan(loan.id)}
                              disabled={busyAction === `return-${loan.id}`}
                            >
                              {busyAction === `return-${loan.id}` ? t('user.active.returning') : t('user.active.return')}
                            </Button>
                          }
                        >
                          <ListItemText
                            primary={borrowedEquipment ? borrowedEquipment.name : t('user.active.fallback', { id: loan.equipmentId })}
                            secondary={
                              borrowedEquipment
                                ? `${borrowedEquipment.type} - ${t('user.active.borrowedOn', { date: new Date(loan.loanDate).toLocaleDateString() })}`
                                : t('user.active.borrowedOn', { date: new Date(loan.loanDate).toLocaleDateString() })
                            }
                          />
                        </ListItem>
                      </Stack>
                    )
                  })}
                </List>
              )}
            </Stack>
          </CardContent>
        </Card>

        <Button variant="text" onClick={logout} sx={{ alignSelf: 'flex-start' }}>
          {t('common.logout')}
        </Button>
      </Stack>

      <AppSnackbar
        open={snackbar !== null}
        message={snackbar?.message ?? ''}
        severity={snackbar?.severity ?? 'info'}
        onClose={() => setSnackbar(null)}
      />
    </Container>
  )
}
