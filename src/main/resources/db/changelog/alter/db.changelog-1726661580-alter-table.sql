ALTER TABLE PUBLIC.Users
DROP COLUMN email_received_at,
ADD COLUMN is_email_sent BOOLEAN,
ADD COLUMN is_email_opened BOOLEAN,
ADD COLUMN is_facebook_link_clicked BOOLEAN,
ADD COLUMN is_insta_link_clicked BOOLEAN,
ADD COLUMN is_twitter_link_clicked BOOLEAN,
ADD COLUMN is_linked_in_link_clicked BOOLEAN,
ADD COLUMN is_News_Room_clicked BOOLEAN;;