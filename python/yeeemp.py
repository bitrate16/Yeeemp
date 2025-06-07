
import traceback
import sqlite3
import os


class DBUtil:
    def init(conn: sqlite3.Connection):
        queries = [
            "CREATE TABLE IF NOT EXISTS tag (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    queue_id INTEGER," +
            "    name TEXT" +
            ");",

            "CREATE INDEX IF NOT EXISTS tag__queue_id ON tag(queue_id);",

            "CREATE UNIQUE INDEX IF NOT EXISTS tag__queue_id_name ON tag(queue_id, name);",

            "CREATE INDEX IF NOT EXISTS tag__name ON tag(name);",

            "CREATE TABLE IF NOT EXISTS event (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    timestamp INTEGER," +
            "    comment TEXT" +
            ");",

            "CREATE TABLE IF NOT EXISTS queue (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    name TEXT" +
            ");",

            "CREATE TABLE IF NOT EXISTS event_tag (" +
            "    event_id INTEGER," +
            "    tag_id INTEGER" +
            ");",

            "CREATE INDEX IF NOT EXISTS event_tag__event_id_tag_id ON event_tag(event_id, tag_id);",

            "CREATE INDEX IF NOT EXISTS event_tag__event_id ON event_tag(event_id);",

            "CREATE INDEX IF NOT EXISTS event_tag__tag_id ON event_tag(tag_id);",

            "CREATE TABLE IF NOT EXISTS queue_event (" +
            "    queue_id INTEGER," +
            "    event_id INTEGER" +
            ");",

            "CREATE INDEX IF NOT EXISTS queue_event__event_id_tag_id ON queue_event(queue_id, event_id);",

            "CREATE INDEX IF NOT EXISTS queue_event__event_id ON queue_event(queue_id);",

            "CREATE INDEX IF NOT EXISTS queue_event__tag_id ON queue_event(event_id);",

            "VACUUM;"
        ]

        for q in queries:
            conn.execute(q)

    def drop(file: str):
        try:
            os.remove(file)
        except:
            traceback.print_exc()


class Util:
    def entity_create(
        conn: sqlite3.Connection,
        table: str,
        key_list: list[str],
        value_list: list,
    ) -> int:
        if len(key_list) != len(value_list):
            raise ValueError(f'len(key_list) != len(value_list): { len(key_list) } != { len(value_list) }')

        if len(key_list) == 0:
            cursor = conn.execute(f'INSERT OR IGNORE INTO { table } (id) VALUES (null)')
        else:
            cursor = conn.execute(f'INSERT OR IGNORE INTO { table } ({ ", ".join(key_list) }) VALUES ({ ", ".join("?" * len(key_list)) })', value_list)

        if cursor.rowcount != 0:
            return cursor.lastrowid

        return None

    def entity_delete(
        conn: sqlite3.Connection,
        table: str,
        id: int,
    ):
        conn.execute(f'DELETE FROM { table } WHERE id = ?', (id,))

    def entity_list(
        conn: sqlite3.Connection,
        table: str,
    ):
        query_result = conn.execute(f'SELECT id FROM { table }').fetchall()

        if not query_result or not query_result[0]:
            return []

        return [ qr[0] for qr in query_result ]

    def set_field(
        conn: sqlite3.Connection,
        table: str,
        id: int,
        name: str,
        value
    ):
        conn.execute(f'UPDATE { table } SET { name } = ? WHERE id = ?', (value, id,))

    def get_field(
        conn: sqlite3.Connection,
        table: str,
        id: int,
        name: str
    ):
        query_result = conn.execute(f'SELECT { name } FROM { table } WHERE id = ?', (id,)).fetchall()

        if not query_result or not query_result[0]:
            return None

        return query_result[0][0]

    def get_id_by_fields(
        conn: sqlite3.Connection,
        table: str,
        key_list: list[str],
        value_list: list,
    ):
        query_result = conn.execute(f'SELECT id FROM { table } WHERE { " AND ".join(f"{ key } = ?" for key in key_list) }', value_list).fetchall()

        if not query_result or not query_result[0]:
            return None

        return query_result[0][0]

    def join_get(
            conn: sqlite3.Connection,
            table: str,
            key_left: str,
            key_right: str,
            id_left: int,
        ) -> list[int]:
        query_result = conn.execute(f'SELECT { key_right } FROM { table } WHERE { key_left } = ?', (id_left,)).fetchall()

        if not query_result or not query_result[0]:
            return []

        return [ q[0] for q in query_result ]

    def join_set(
        conn: sqlite3.Connection,
        table: str,
        key_left: str,
        key_right: str,
        id_left: int,
        id_right: int,
    ):
        conn.execute(f'INSERT OR IGNORE INTO { table } ({ key_left }, { key_right }) VALUES (?, ?)', (id_left, id_right))

    def join_delete(
        conn: sqlite3.Connection,
        table: str,
        key_left: str,
        key_right: str,
        id_left: int,
        id_right: int,
    ):
        conn.execute(f'DELETE FROM { table } WHERE { key_left } = ? AND { key_right } = ?', (id_left, id_right))

    def join_delete_all(
        conn: sqlite3.Connection,
        table: str,
        key_left: str,
        id_left: int,
    ):
        conn.execute(f'DELETE FROM { table } WHERE { key_left } = ?', (id_left,))

    def cleanup(
        conn: sqlite3.Connection,
    ):
        conn.executescript("""
            -- Step 1: delete relations with noexistent queues
            delete from queue_event where queue_id not in (select distinct id from queue);

            -- Step 2: delete relations with noexistent events
            delete from queue_event where event_id not in (select distinct id from event);
            delete from event_tag where event_id not in (select distinct id from event);

            -- Step 3: delete relations with noexistent tags
            delete from event_tag where tag_id not in (select distinct id from tag);

            -- Step 4: drop all unused events
            delete from event where id not in (select distinct event_id from queue_event);
            delete from event_tag where event_id not in (select distinct event_id from queue_event);

            -- Step 5: drop all unused tags
            delete from tag where id not in (select distinct tag_id from event_tag);
        """)


class BaseEntity:
    """
    Represents entity in database mapped to table.

    Each entity has unique `id`.
    """

    def __init__(
        self,
        conn: sqlite3.Connection,
        id: int,
    ):
        self.conn = conn
        self.id = id

    def get_conn(self) -> sqlite3.Connection:
        return self.conn

    def get_id(self) -> int:
        return self.id

    def get_table() -> str:
        raise NotImplementedError()


class Queue(BaseEntity):
    TABLE_NAME = 'queue'

    def get_table(self):
        return Queue.TABLE_NAME

    def get_name(self) -> str:
        return Util.get_field(self.get_conn(), Queue.TABLE_NAME, self.get_id(), 'name')

    def set_name(self, name: str):
        Util.set_field(self.get_conn(), Queue.TABLE_NAME, self.get_id(), 'name', name)

    def __str__(self) -> str:
        return f'Queue(id={ self.get_id() }, name="{ self.get_name() }")'

    __repr__ = __str__


class Event(BaseEntity):
    TABLE_NAME = 'event'

    def get_table(self):
        return Event.TABLE_NAME

    def get_timestamp(self) -> int:
        return Util.get_field(self.get_conn(), Event.TABLE_NAME, self.get_id(), 'timestamp')

    def set_timestamp(self, timestamp: str):
        Util.set_field(self.get_conn(), Event.TABLE_NAME, self.get_id(), 'timestamp', timestamp)

    def get_comment(self) -> str:
        return Util.get_field(self.get_conn(), Event.TABLE_NAME, self.get_id(), 'comment')

    def set_comment(self, comment: str):
        Util.set_field(self.get_conn(), Event.TABLE_NAME, self.get_id(), 'comment', comment)

    def __str__(self) -> str:
        return f'Event(id={ self.get_id() }, timestamp={ self.get_timestamp() }, comment="{ self.get_comment() }")'

    __repr__ = __str__


class Tag(BaseEntity):
    TABLE_NAME = 'tag'

    def get_name(self) -> str:
        return Util.get_field(self.get_conn(), Tag.TABLE_NAME, self.get_id(), 'name')

    def get_queue_id(self) -> int:
        return Util.get_field(self.get_conn(), Tag.TABLE_NAME, self.get_id(), 'queue_id')

    def set_name(self, name: str):
        Util.set_field(self.get_conn(), Tag.TABLE_NAME, self.get_id(), 'name', name)

    def __str__(self) -> str:
        return f'Tag(id={ self.get_id() }, name="{ self.get_name() }")'

    __repr__ = __str__


class Root:
    def _get_join_table(table_left: str, table_right: str) -> str:
        return f'{ table_left }_{ table_right }'

    def _get_id_key(table: str) -> str:
        return f'{ table }_id'

    def __init__(
        self,
        conn: sqlite3.Connection,
    ):
        self.conn = conn

    def get_conn(self) -> sqlite3.Connection:
        return self.conn

    def list_queue(self) -> list[Queue]:
        ids = Util.entity_list(
            self.conn,
            Queue.TABLE_NAME,
        )

        return [
            Queue(
                self.conn,
                id
            )
            for id in ids
        ]

    def create_queue(self) -> Queue:
        return Queue(
            self.get_conn(),
            Util.entity_create(
                self.get_conn(),
                Queue.TABLE_NAME,
                [],
                [],
            )
        )

    def create_tag(self, queue: Queue, name: str) -> Tag:
        id = Util.entity_create(
            self.get_conn(),
            Tag.TABLE_NAME,
            [ Root._get_id_key(Queue.TABLE_NAME), 'name' ],
            [ queue.get_id(), name ],
        )

        if id is None:
            return None

        return Tag(
            self.get_conn(),
            id,
        )

    def get_tag_by_name(self, queue: Queue, name: str) -> Tag:
        id = Util.get_id_by_fields(
            self.get_conn(),
            Tag.TABLE_NAME,
            [ 'queue_id', 'name' ],
            [ queue.get_id(), name ],
        )

        return Tag(
            self.get_conn(),
            id,
        )

    def force_create_tag(self, queue: Queue, name: str) -> Tag:
        tag = self.create_tag(queue, name)
        if tag is None:
            return self.get_tag_by_name(queue, name)
        return tag

    def list_queue_tag(self, queue: Queue) -> list[Tag]:
        tag_ids = Util.join_get(
            self.get_conn(),
            'tag',
            Root._get_id_key(Queue.TABLE_NAME),
            'id',
            queue.get_id(),
        )

        return [
            Tag(
                self.get_conn(),
                tid,
            )
            for tid in tag_ids
        ]

    def create_event(self) -> Event:
        id = Util.entity_create(
            self.get_conn(),
            Event.TABLE_NAME,
            [],
            [],
        )

        return Event(
            self.get_conn(),
            id,
        )

    def join_queue_event(self, queue: Queue, event: Event):
        Util.join_set(
            self.get_conn(),
            Root._get_join_table(Queue.TABLE_NAME, Event.TABLE_NAME),
            Root._get_id_key(Queue.TABLE_NAME),
            Root._get_id_key(Event.TABLE_NAME),
            queue.get_id(),
            event.get_id(),
        )

    def list_queue_event(self, queue: Queue) -> list[Event]:
        ids = Util.join_get(
            self.get_conn(),
            Root._get_join_table(Queue.TABLE_NAME, Event.TABLE_NAME),
            Root._get_id_key(Queue.TABLE_NAME),
            Root._get_id_key(Event.TABLE_NAME),
            queue.get_id(),
        )

        return [
            Event(
                self.get_conn(),
                id,
            )
            for id in ids
        ]

    def list_event_tag(self, event: Event) -> list[Tag]:
        ids = Util.join_get(
            self.get_conn(),
            Root._get_join_table(Event.TABLE_NAME, Tag.TABLE_NAME),
            Root._get_id_key(Event.TABLE_NAME),
            Root._get_id_key(Tag.TABLE_NAME),
            event.get_id(),
        )

        return [
            Tag(
                self.get_conn(),
                id,
            )
            for id in ids
        ]

    def join_event_tag(self, event: Event, tag: Tag):
        Util.join_set(
            self.get_conn(),
            Root._get_join_table(Event.TABLE_NAME, Tag.TABLE_NAME),
            Root._get_id_key(Event.TABLE_NAME),
            Root._get_id_key(Tag.TABLE_NAME),
            event.get_id(),
            tag.get_id(),
        )

    def delete_queue_event(self, queue: Queue, event: Event):
        Util.join_delete(
            self.get_conn(),
            Root._get_join_table(Queue.TABLE_NAME, Event.TABLE_NAME),
            Root._get_id_key(Queue.TABLE_NAME),
            Root._get_id_key(Event.TABLE_NAME),
            queue.get_id(),
            event.get_id(),
        )

    def delete_event_tag(self, event: Event, tag: Tag):
        Util.join_delete(
            self.get_conn(),
            Root._get_join_table(Event.TABLE_NAME, Tag.TABLE_NAME),
            Root._get_id_key(Event.TABLE_NAME),
            Root._get_id_key(Tag.TABLE_NAME),
            event.get_id(),
            tag.get_id(),
        )

    def delete_event_tag_all(self, event: Event):
        Util.join_delete_all(
            self.get_conn(),
            Root._get_join_table(Event.TABLE_NAME, Tag.TABLE_NAME),
            Root._get_id_key(Event.TABLE_NAME),
            event.get_id(),
        )

    def cleanup(self):
        Util.cleanup(self.conn)
