INSERT INTO BOOKING_STATUSES(NAME, DESCRIPTION)
VALUES ('WAITING', 'Ожидает подтверждения');
INSERT INTO BOOKING_STATUSES(NAME, DESCRIPTION)
VALUES ('APPROVED', 'Подтверждено владельцем');
INSERT INTO BOOKING_STATUSES(NAME, DESCRIPTION)
VALUES ('REJECTED', 'Отклонено владельцем');

delete from bookings where id = 2000000;

