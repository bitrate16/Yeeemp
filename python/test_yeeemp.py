import sqlite3
import typing

import yeeemp


def create_db():
    conn = sqlite3.connect(':memory:')
    # yeeemp.DBUtil.drop('test.db')
    # conn = sqlite3.connect('test.db')
    yeeemp.DBUtil.init(conn)

    return conn


def test_queue_create():
    with create_db() as conn:
        root = yeeemp.Root(conn)

        queue_list = root.list_queue()
        assert len(queue_list) == 0, 'expected empty queue list'

        queue = root.create_queue()
        assert queue.get_id() == 1, ''
        assert queue.get_name() is None, 'no name set by default'

        queue.set_name('aboba')
        assert queue.get_name() == 'aboba', ''

        queue_list = root.list_queue()
        assert len(queue_list) == 1, 'expected not empty queue list'


def test_queue_tag_create():
    with create_db() as conn:
        root = yeeemp.Root(conn)

        queue = root.create_queue()
        queue.set_name('aboba')

        queue_tags = root.list_queue_tag(queue)
        assert len(queue_tags) == 0, 'expected empty tags list for new queue'

        tag = root.create_tag(queue, 'gato')
        assert tag is not None, ''

        tag_lookup = root.get_tag_by_name(queue, 'gato')
        assert tag_lookup is not None, 'expected find created tag'
        assert tag.get_id() == tag_lookup.get_id() is not None, 'expected tag id match'

        tag = root.create_tag(queue, 'pato')
        assert tag is not None, ''

        tag = root.create_tag(queue, 'gato')
        assert tag is None, 'expected fail on duplicating tags'

        queue_tags = root.list_queue_tag(queue)
        assert len(queue_tags) == 2, 'expected not empty tags list for new queue'

        queue = root.create_queue()
        queue.set_name('bebra')

        queue_tags = root.list_queue_tag(queue)
        assert len(queue_tags) == 0, 'expected empty tags list for new queue'


def create_tags(root: yeeemp.Root, queue: yeeemp.Queue, prefix: str, n: int) -> list[yeeemp.Tag]:
    tags = []

    for idx in range(n):
        tags.append(root.create_tag(queue, f'{ prefix }_{ idx }'))

    return tags


def force_create_tags(root: yeeemp.Root, queue: yeeemp.Queue, prefix: str, n: int) -> list[yeeemp.Tag]:
    tags = []

    for idx in range(n):
        tags.append(root.force_create_tag(queue, f'{ prefix }_{ idx }'))

    return tags


def subproc_event_add_tags(
    root: yeeemp.Root,
    queue: yeeemp.Queue,
    event: yeeemp.Event,
    tags: list[yeeemp.Tag],
):
    for tag in tags:
        root.join_event_tag(event, tag)


def subproc_event_drop_tags(
    root: yeeemp.Root,
    queue: yeeemp.Queue,
    event: yeeemp.Event,
):
    root.delete_event_tag_all(event)


def subproc_event_check_no_tags(
    root: yeeemp.Root,
    queue: yeeemp.Queue,
    event: yeeemp.Event,
):
    event_tags = root.list_event_tag(event)

    assert len(event_tags) == 0, 'expected no tags'


def subproc_event_check_has_tags(
    root: yeeemp.Root,
    queue: yeeemp.Queue,
    event: yeeemp.Event,
    tags: list[yeeemp.Tag],
):
    "Check has all specified tags"

    event_tags = root.list_event_tag(event)
    event_tag_set = set([ tag.get_name() for tag in event_tags ])

    assert all([ tag.get_name() in event_tag_set for tag in tags ]), 'event missing expected tags'


def subproc_event_check_has_only_tags(
    root: yeeemp.Root,
    queue: yeeemp.Queue,
    event: yeeemp.Event,
    tags: list[yeeemp.Tag],
):
    "Check has only specified tags"

    event_tags = root.list_event_tag(event)
    event_tag_set = set([ tag.get_name() for tag in event_tags ])
    expected_tag_set = set([ tag.get_name() for tag in tags ])

    assert all([ tag.get_name() in expected_tag_set for tag in event_tags ]), 'event has unexpected tags tags'
    assert all([ tag.get_name() in event_tag_set for tag in tags ]), 'event missing expected tags tags'


def subproc_map_tags_sample(
    master_prefix: str,
    mapper: typing.Callable[[str, int], None]
):

    # append event tags
    prefixes = [
        master_prefix + 'bread',
        master_prefix + 'gato',
        master_prefix + 'potato',
        master_prefix + 'aboba',
        master_prefix + 'stonks'
    ]

    populations = [
        1,
        102,
        9127,
        0,
        2,
    ]

    for (prefix, population) in zip(prefixes, populations):
        mapper(prefix, population)


def test_event_create():
    with create_db() as conn:
        root = yeeemp.Root(conn)

        queue = root.create_queue()
        queue.set_name('aboba')

        queue_events = root.list_queue_event(queue)
        assert len(queue_events) == 0, 'expected empty event list in new queue'

        event = root.create_event()

        queue_events = root.list_queue_event(queue)
        assert len(queue_events) == 0, 'expected empty event list because event is not attached'

        event.set_comment('uwu')
        assert event.get_comment() == 'uwu'
        event.set_comment('owo')
        assert event.get_comment() == 'owo'

        event.set_timestamp(1029384756)
        assert event.get_timestamp() == 1029384756
        event.set_timestamp(732)
        assert event.get_timestamp() == 732

        # append event to queue
        root.join_queue_event(queue, event)

        queue_events = root.list_queue_event(queue)
        assert len(queue_events) == 1, 'expected not empty event list in queue'

        # Add & delete tags repeatedly
        def clean_add_tags(prefix, population):
            tags = force_create_tags(
                root=root,
                queue=queue,
                prefix=prefix,
                n=population,
            )

            subproc_event_add_tags(
                root,
                queue,
                event,
                tags,
            )

            subproc_event_check_has_only_tags(
                root,
                queue,
                event,
                tags
            )

            subproc_event_drop_tags(root, queue, event)

        subproc_map_tags_sample(
            'owo_',
            clean_add_tags
        )

        # Add new tags & expect persisting
        prev_tags = None
        def dirty_add_tags(prefix, population):
            nonlocal prev_tags

            tags = force_create_tags(
                root=root,
                queue=queue,
                prefix=prefix,
                n=population,
            )

            subproc_event_add_tags(
                root,
                queue,
                event,
                tags,
            )

            if prev_tags is not None:
                subproc_event_check_has_tags(
                    root,
                    queue,
                    event,
                    prev_tags
                )

            prev_tags = tags

        subproc_map_tags_sample(
            'uwu_',
            dirty_add_tags
        )

        # New event with other tags must not mess with other events
        event = root.create_event()
        root.join_queue_event(queue, event)

        tags = force_create_tags(
            root=root,
            queue=queue,
            prefix='rawr',
            n=17,
        )

        subproc_event_add_tags(
            root,
            queue,
            event,
            tags,
        )

        subproc_event_check_has_only_tags(
            root,
            queue,
            event,
            tags
        )
