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
  Typography,
} from '@mui/material'
import { useEffect, useMemo, useState } from 'react'
import { equipmentService } from '../../../services/equipmentService'
import { loanService } from '../../../services/loanService'
import { useAuth } from '../../auth/context/AuthContext'
import type { Equipment } from '../../../shared/types/domain'
import { AppSnackbar } from '../../../shared/ui/AppSnackbar'
import { SectionHeader } from '../../../shared/ui/SectionHeader'

export const UserDashboardPage = () => {
  const { session, logout } = useAuth()
  const [equipment, setEquipment] = useState<Equipment[]>([])
  const [selectedEquipmentId, setSelectedEquipmentId] = useState('')
  const [busyAction, setBusyAction] = useState<string | null>(null)
  const [snackbar, setSnackbar] = useState<{
    message: string
    severity: 'success' | 'error' | 'info' | 'warning'
  } | null>(null)

  const availableEquipment = useMemo(
    () => equipment.filter((item) => item.status === 'AVAILABLE'),
    [equipment],
  )

  useEffect(() => {
    const loadEquipment = async () => {
      try {
        const response = await equipmentService.getAll()
        setEquipment(response)
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Unable to load equipment.'
        setSnackbar({ message, severity: 'error' })
      }
    }

    void loadEquipment()
  }, [])

  useEffect(() => {
    if (!selectedEquipmentId && availableEquipment.length > 0) {
      setSelectedEquipmentId(String(availableEquipment[0].id))
    }
  }, [availableEquipment, selectedEquipmentId])

  const submitLoan = async () => {
    if (!session) {
      return
    }

    const parsedEquipmentId = Number(selectedEquipmentId)
    if (Number.isNaN(parsedEquipmentId) || parsedEquipmentId <= 0) {
      setSnackbar({ message: 'Select an available item before creating the loan.', severity: 'warning' })
      return
    }

    setBusyAction('loan')
    try {
      await loanService.create({
        userId: session.userId,
        equipmentId: parsedEquipmentId,
      })
      setSnackbar({ message: 'Your loan request was created successfully.', severity: 'success' })
      const refreshed = await equipmentService.getAll()
      setEquipment(refreshed)
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to create loan.'
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
                title="Loan equipment"
                subtitle="Pick an available item and we’ll create the loan for your account."
              />

              {availableEquipment.length === 0 ? (
                <Card variant="outlined" sx={{ backgroundColor: 'background.default' }}>
                  <CardContent>
                    <Stack spacing={1}>
                      <Typography variant="h6">Nothing available right now</Typography>
                      <Typography color="text.secondary">
                        All equipment is currently reserved or under maintenance. Check back later or refresh the list.
                      </Typography>
                    </Stack>
                  </CardContent>
                </Card>
              ) : (
                <>
                  <FormControl fullWidth>
                    <InputLabel id="equipment-loan-select">Available equipment</InputLabel>
                    <Select
                      labelId="equipment-loan-select"
                      label="Available equipment"
                      value={selectedEquipmentId}
                      onChange={(event) => setSelectedEquipmentId(event.target.value)}
                    >
                      {availableEquipment.map((item) => (
                        <MenuItem value={String(item.id)} key={item.id}>
                          {item.name} <Chip size="small" label={item.type} sx={{ ml: 1 }} />
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <Button
                    variant="contained"
                    onClick={() => void submitLoan()}
                    disabled={busyAction === 'loan'}
                  >
                    {busyAction === 'loan' ? 'Creating loan...' : 'Borrow selected equipment'}
                  </Button>
                </>
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
