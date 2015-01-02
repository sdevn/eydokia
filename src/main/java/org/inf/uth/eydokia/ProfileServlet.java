
package org.inf.uth.eydokia;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import static org.inf.uth.eydokia.jooq.tables.Entry.ENTRY;
import static org.inf.uth.eydokia.jooq.tables.Room.ROOM;
import static org.inf.uth.eydokia.jooq.tables.Schedule.SCHEDULE;
import static org.inf.uth.eydokia.jooq.tables.ScheduleType.SCHEDULE_TYPE;
import static org.inf.uth.eydokia.jooq.tables.User.USER;
import org.inf.uth.eydokia.jooq.tables.records.RoomRecord;
import org.inf.uth.eydokia.jooq.tables.records.ScheduleRecord;
import org.inf.uth.eydokia.jooq.tables.records.ScheduleTypeRecord;
import org.inf.uth.eydokia.jooq.tables.records.UserRecord;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 *
 * @author Nilos
 */
public class ProfileServlet extends HttpServlet {
    
    @Resource(name = "jdbc/eydokia")
    private DataSource mDataSource;

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection conn = mDataSource.getConnection())
        {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
            
            String username = request.getParameter("username");
            
            UserRecord user = create.selectFrom(USER)
                            .where(USER.USERNAME.eq(username))
                        .fetchOne();
            
            Result entries = create.selectFrom(ENTRY)
                            .where(ENTRY.USER_ID.eq(user.getUserId()))
                        .fetch();
            Map<Integer, RoomRecord> rooms = create.selectFrom(ROOM)
                            .where(ROOM.ROOM_ID.in(entries.getValues(ENTRY.ROOM_ID)))
                        .fetchMap(ROOM.ROOM_ID);
            
            Map<Integer, UserRecord> users = new HashMap<>();
            users.put(user.getUserId(), user);
            
            Map<Integer, ScheduleRecord> schedules = create.selectFrom(SCHEDULE)
                            .where(SCHEDULE.SCHEDULE_ID.in(entries.getValues(ENTRY.SCHEDULE_ID)))
                        .fetchMap(SCHEDULE.SCHEDULE_ID);
            
            Map<Integer, ScheduleTypeRecord> scheduleTypes = create.selectFrom(SCHEDULE_TYPE)
                                .fetchMap(SCHEDULE_TYPE.SCHEDULE_TYPE_ID);
            
            request.setAttribute("entries", entries);  
            request.setAttribute("rooms", rooms);  
            request.setAttribute("users", users);  
            request.setAttribute("schedules", schedules);  
            request.setAttribute("schedule_types", scheduleTypes);              
            request.setAttribute("user", user);  
            
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
        }
        catch (Exception e)
        {
            
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
