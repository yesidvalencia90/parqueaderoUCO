package co.com.k4soft.parqueaderouco.persistencia.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import co.com.k4soft.parqueaderouco.entidades.Movimiento;
import co.com.k4soft.parqueaderouco.entidades.Tarifa;

@Dao
public interface MovimientoDAO {

    @Query("SELECT * FROM MOVIMIENTO Where placa=:placa AND finalizaMovimiento = 0")
    Movimiento  findByPLaca(String placa);

    @Query("SELECT * FROM MOVIMIENTO Where idMovimiento=:idMovimiento")
    Movimiento  findById(int idMovimiento);

    @Insert
    void insert(Movimiento movimiento);

    @Query("UPDATE MOVIMIENTO SET fechaSalida=:fechaSalida, finalizaMovimiento = 1, totalPagado=:totalPagado WHERE idMovimiento=:idMovimiento")
    void updateSalida(String fechaSalida, double totalPagado, int idMovimiento);

    @Query("SELECT * FROM MOVIMIENTO WHERE finalizaMovimiento = 1 AND strftime(fechaEntrada) >= strftime(:inicio) AND strftime(fechaSalida) <= strftime(:Final)")

    List<Movimiento> getMovimientos(String inicio, String Final);

    @Query("UPDATE MOVIMIENTO SET fechaSalida='2020-10-15 15:40:15', fechaEntrada = '2020-10-14 09:45:45', totalPagado = 16000 WHERE idMovimiento = 1")
    void updateTest();
}
