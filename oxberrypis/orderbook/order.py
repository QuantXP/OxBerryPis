"""Order implementation.

Created on Apr 28, 2013

.. codeauthor:: Hynek Jemelik

"""


class Order(object):
    def __init__(self, order_id, limit_price, num_shares, order_type):
        self.id = order_id
        self.price = limit_price
        self.num_shares = num_shares
        self.type = order_type

    def key(self):
        if self.type == "sell":
            return self.price
        else:
            return - self.price
