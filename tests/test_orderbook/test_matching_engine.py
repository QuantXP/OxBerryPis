import unittest


class DummyMatchingEngineCallback(object):

    def __init__(self):
        self.trades = []

    def trade(self, num_shares, price):
        self.trades.append((num_shares, price))


class TestMatchingEngine(unittest.TestCase):

    def get_cls(self):
        from oxberrypis.orderbook.matching_engine import MatchingEngine
        return MatchingEngine

    def setUp(self):
        self.cls = self.get_cls()

    def test_it(self):
        from oxberrypis.orderbook.order import Order

        callback = DummyMatchingEngineCallback()
        s = self.cls(callback)

        s.add_order(1, 10, 7000, Order.BUY)
        # 10: -7000
        s.add_order(2, 10, 1000, Order.SELL)

        # Trading 1000 shares at price 10.
        self.assertEqual(len(callback.trades), 1)
        self.assertEqual(callback.trades[-1], (1000, 10))

        # 10: -6000
        s.add_order(3, 12, 9000, Order.SELL)
        # 12 +9000, 10: -6000
        s.add_order(4, 11, 1000, Order.SELL)
        # 12 +9000, 11: +1000, 10: -6000
        s.update_order(3, 12, 6000, Order.SELL)
        # 12: +6000, 11: +1000, 10: -6000
        s.add_order(5, 12, 8000, Order.BUY)

        # Trading 1000 shares at price 11.
        # Trading 6000 shares at price 12.
        self.assertEqual(len(callback.trades), 3)
        self.assertEqual(callback.trades[-2], (1000, 11))
        self.assertEqual(callback.trades[-1], (6000, 12))

        # 12: -1000, 10: -6000
        s.add_order(6, 12, 2000, Order.BUY)
        # 12: -3000, 10: -6000
        s.add_order(7, 12, 3000, Order.BUY)
        # 12: -6000, 10: -6000
        s.update_order(6, 12, 4000, Order.BUY)
        # 12: -8000, 10: -6000
        s.add_order(8, 13, 6000, Order.SELL)
        # 13: +6000, 12: -8000, 10: -6000
        s.add_order(9, 12, 7000, Order.SELL)

        # Trading 1000 shares at price 12.
        # Trading 3000 shares at price 12.
        # Trading 3000 shares at price 12.
        self.assertEqual(len(callback.trades), 6)
        self.assertEqual(callback.trades[-3], (1000, 12))
        self.assertEqual(callback.trades[-2], (3000, 12))
        self.assertEqual(callback.trades[-1], (3000, 12))

        # 13: +6000, 12: -1000, 10: -6000
        s.add_order(10, 11, 2000, Order.SELL)

        # Trading 1000 shares at price 12.
        self.assertEqual(len(callback.trades), 7)
        self.assertEqual(callback.trades[-1], (1000, 12))

        # 13: +6000, 11: +1000, 10: -6000
        s.add_order(11, 11, 3000, Order.BUY)

        # Trading 1000 shares at price 11.
        self.assertEqual(len(callback.trades), 8)
        self.assertEqual(callback.trades[-1], (1000, 11))

        # 13: +6000, 11: -2000, 10: -6000
        s.remove_order(11)
        # 13: +6000, 10: -6000
        s.add_order(12, 8, 3000, Order.SELL)

        # Trading 3000 shares at price 10.
        self.assertEqual(len(callback.trades), 9)
        self.assertEqual(callback.trades[-1], (3000, 10))

        # 13: +6000, 10: -3000
