-- ============================================
-- Script de création de la table location_history
-- Pour FyourF Application
-- ============================================
-- 
-- INSTRUCTIONS :
-- 1. Allez sur https://supabase.com/dashboard/project/skbttjztscyebsrvghqu
-- 2. Cliquez sur "SQL Editor" dans le menu de gauche
-- 3. Cliquez sur "New query"
-- 4. Copiez-collez tout ce script
-- 5. Cliquez sur "Run" ou appuyez sur Ctrl+Enter
-- 6. Vérifiez que tout s'est bien passé (pas d'erreurs)
-- 7. Allez dans "Table Editor" pour voir la table créée
--
-- ============================================

-- Étape 1 : Supprimer la table si elle existe déjà (optionnel)
DROP TABLE IF EXISTS location_history CASCADE;

-- Étape 2 : Créer la table location_history
CREATE TABLE location_history (
    id BIGSERIAL PRIMARY KEY,
    phone TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Étape 3 : Créer les index pour optimiser les performances
-- Index sur le numéro de téléphone pour des recherches rapides
CREATE INDEX idx_location_history_phone ON location_history(phone);

-- Index sur le timestamp pour trier par date
CREATE INDEX idx_location_history_timestamp ON location_history(timestamp DESC);

-- Index unique pour éviter les doublons de téléphone (un seul enregistrement par téléphone)
CREATE UNIQUE INDEX idx_location_history_phone_unique ON location_history(phone);

-- Étape 4 : Activer Row Level Security (RLS)
ALTER TABLE location_history ENABLE ROW LEVEL SECURITY;

-- Étape 5 : Supprimer les anciennes politiques si elles existent
DROP POLICY IF EXISTS "Allow public read access" ON location_history;
DROP POLICY IF EXISTS "Allow public insert access" ON location_history;
DROP POLICY IF EXISTS "Allow public update access" ON location_history;
DROP POLICY IF EXISTS "Allow public delete access" ON location_history;

-- Étape 6 : Créer les politiques de sécurité
-- Politique pour permettre la lecture avec la clé anon
CREATE POLICY "Allow public read access" ON location_history
    FOR SELECT
    USING (true);

-- Politique pour permettre l'insertion avec la clé anon
CREATE POLICY "Allow public insert access" ON location_history
    FOR INSERT
    WITH CHECK (true);

-- Politique pour permettre la mise à jour avec la clé anon
CREATE POLICY "Allow public update access" ON location_history
    FOR UPDATE
    USING (true);

-- Politique pour permettre la suppression avec la clé anon
CREATE POLICY "Allow public delete access" ON location_history
    FOR DELETE
    USING (true);

-- Étape 7 : Insérer des données de test
INSERT INTO location_history (phone, latitude, longitude, timestamp)
VALUES 
    ('+1234567890', 36.8065, 10.1815, EXTRACT(EPOCH FROM NOW())::BIGINT * 1000),
    ('+0987654321', 36.8500, 10.2000, EXTRACT(EPOCH FROM NOW())::BIGINT * 1000);

-- Étape 8 : Vérifier que les données ont été insérées
SELECT 
    id,
    phone,
    latitude,
    longitude,
    timestamp,
    created_at,
    to_timestamp(timestamp / 1000) as timestamp_readable
FROM location_history
ORDER BY timestamp DESC;

-- ============================================
-- RÉSULTAT ATTENDU :
-- ============================================
-- Vous devriez voir :
-- ✅ Table 'location_history' créée avec succès
-- ✅ 3 index créés (phone, timestamp, phone_unique)
-- ✅ RLS activé
-- ✅ 4 politiques créées (read, insert, update, delete)
-- ✅ 2 lignes de données de test insérées
-- ✅ Un tableau avec les 2 enregistrements de test
--
-- Si vous voyez des erreurs, vérifiez :
-- - Que vous êtes bien connecté à votre projet Supabase
-- - Que vous avez les permissions nécessaires
-- - Que le nom de la table n'existe pas déjà
-- ============================================

-- COMMANDES UTILES POUR LE DEBUG :
-- ============================================

-- Voir toutes les tables
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

-- Voir la structure de la table
-- SELECT column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'location_history';

-- Voir tous les index
-- SELECT indexname, indexdef 
-- FROM pg_indexes 
-- WHERE tablename = 'location_history';

-- Voir toutes les politiques RLS
-- SELECT * FROM pg_policies WHERE tablename = 'location_history';

-- Compter le nombre d'enregistrements
-- SELECT COUNT(*) FROM location_history;

-- Supprimer toutes les données (ATTENTION !)
-- DELETE FROM location_history;

-- Supprimer la table complètement (ATTENTION !)
-- DROP TABLE IF EXISTS location_history CASCADE;

