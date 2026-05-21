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
import { useEffect, useMemo, useState } from 'react'
import { equipmentService } from '../../../services/equipmentService'
import { loanService } from '../../../services/loanService'
import { waitingListService } from '../../../services/waitingListService'
import { useAuth } from '../../auth/context/AuthContext'
import type { Equipment, Loan } from '../../../shared/types/domain'
import { AppSnackbar } from '../../../shared/ui/AppSnackbar'
import { SectionHeader } from '../../../shared/ui/SectionHeader'

export const UserDashboardPage = () => {
  const { session, logout } = useAuth()
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

  const loadData = async () => {
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
      const message = error instanceof Error ? error.message : 'Unable to load equipment.'
      setSnackbar({ message, severity: 'error' })
    }
  }

  useEffect(() => {
    void loadData()
  }, [])

  const submitLoan = async () => {
    if (!session || !selectedItem) return

    setBusyAction('loan')
    try {
      await loanService.create({
        userId: session.userId,
        equipmentId: selectedItem.id,
      })
      setSnackbar({ message: 'Your loan request was created successfully.', severity: 'success' })
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to create loan.'
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
        message: `You have successfully joined the waiting list for ${selectedItem.name}!`, 
        severity: 'success' 
      })
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to join the waiting list.'
      setSnackbar({ message, severity: 'error' })
    } finally {
      setBusyAction(null)
    }
  }

  const returnLoan = async (loanId: number) => {
    setBusyAction(`return-${loanId}`)
    try {
      await loanService.returnLoan(loanId)
      setSnackbar({ message: 'The borrowed item was returned successfully.', severity: 'success' })
      await loadData()
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to return the borrowed item.'
      setSnackbar({ message, severity: 'error' })
    } finally {
      setBusyAction(null)
    }
  }

  return (
    <Container maxWidth="md" sx={{ py: 6 }}>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h3">Welcome, {session?.username}</Typography>
          <Typography color="text.secondary">
            This page keeps the borrowing flow simple and focused.
          </Typography>
        </Box>

        <Card>
          <CardContent>
            <Stack spacing={2}>
              <SectionHeader
                title="Equipment Center"
                subtitle="Pick any item to borrow it instantly or join its waiting list if it's currently loaned."
              />

              {equipment.length === 0 ? (
                <Card variant="outlined" sx={{ backgroundColor: 'background.default' }}>
                  <CardContent>
                    <Stack spacing={1}>
                      <Typography variant="h6">No inventory found</Typography>
                      <Typography color="text.secondary">
                        There is no equipment registered in the system database.
                      </Typography>
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
                    noOptionsText="No equipment found"
                    renderInput={(params) => <TextField {...params} label="Select Equipment" />}
                  />

                  {selectedItem && selectedItem.status === 'AVAILABLE' ? (
                    <Button
                      variant="contained"
                      onClick={() => void submitLoan()}
                      disabled={busyAction === 'loan'}
                      fullWidth
                    >
                      {busyAction === 'loan' ? 'Creating loan...' : 'Borrow selected equipment'}
                    </Button>
                  ) : selectedItem ? (
                    <Stack spacing={1} width="100%">
                      <Chip 
                        label={
                          isAlreadyLoanedByMe 
                            ? "You currently have an active borrow for this item" 
                            : selectedItem.status === 'UNDER_MAINTENANCE'
                            ? "This item is currently under maintenance"
                            : "This item is currently loaned to another student"
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
                          ? "Cannot join list (Item already in possession)" 
                          : busyAction === 'waiting' 
                          ? 'Joining list...' 
                          : 'Join the Waiting List'}
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
                title="Your active borrows"
                subtitle="Use these entries to end a borrow and make the equipment available again."
              />

              {activeUserLoans.length === 0 ? (
                <Typography color="text.secondary">
                  You do not have any active borrows right now.
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
                              {busyAction === `return-${loan.id}` ? 'Returning...' : 'End borrow'}
                            </Button>
                          }
                        >
                          <ListItemText
                            primary={borrowedEquipment ? borrowedEquipment.name : `Equipment #${loan.equipmentId}`}
                            secondary={
                              borrowedEquipment
                                ? `${borrowedEquipment.type} · Borrowed on ${new Date(loan.loanDate).toLocaleDateString()}`
                                : `Borrowed on ${new Date(loan.loanDate).toLocaleDateString()}`
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
          Logout
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